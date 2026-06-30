package com.tracetrail.ingestion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartitionScheduler {

    private final JdbcTemplate jdbc;

    @Value("${tracetrail.retention-days}")
    private int retentionDays;

    @Scheduled(cron = "0 0 23 * * *", zone = "UTC")
    public void runMaintenance(){
        LocalDate tomorrow =
                LocalDate
                        .now(ZoneOffset.UTC).plusDays(1);

        String partName = "p" + tomorrow.format(
                DateTimeFormatter.ofPattern("yyyyMMdd")
        );

        long upperNanos = tomorrow.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1_000_000_000L;

        int added = 0, dropped = 0;

        for (String table : List.of("spans", "metric_points", "logs")) {
            // --- extend forward ---
            try {
                jdbc.execute(
                        "ALTER TABLE " + table + " REORGANIZE PARTITION p_future INTO (" +
                                "PARTITION " + partName + " VALUES LESS THAN (" + upperNanos + "), " +
                                "PARTITION p_future VALUES LESS THAN MAXVALUE)"
                );
                added++;
            } catch (Exception e) {
                log.error("reorganize failed for {}", table, e);
            }

            // --- prune backward ---
            List<String> names = jdbc.queryForList(
                    "SELECT PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS " +
                            "WHERE TABLE_SCHEMA='tracetrail' AND TABLE_NAME=?",
                    String.class, table
            );

            LocalDate cutoff = LocalDate.now(ZoneOffset.UTC)
                    .minusDays(retentionDays);

            for (String name : names) {
                if (!name.matches("p\\d{8}")) continue;          // skip p_future etc.

                LocalDate partDate = LocalDate.parse(
                        name.substring(1),                            // strip leading "p"
                        DateTimeFormatter.ofPattern("yyyyMMdd")
                );

                if (partDate.isBefore(cutoff)) {
                    try {
                        jdbc.execute("ALTER TABLE " + table + " DROP PARTITION " + name);
                        dropped++;
                    } catch (Exception e) {
                        log.error("drop partition failed for {} {}", table, name, e);
                    }
                }
            }
        }

        log.info("partition maintenance: added={} dropped={} per table", added, dropped);
    }

}
