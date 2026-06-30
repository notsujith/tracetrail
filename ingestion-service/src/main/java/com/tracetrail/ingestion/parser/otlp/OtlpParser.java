package com.tracetrail.ingestion.parser.otlp;

import com.tracetrail.ingestion.parser.Attributes;
import com.tracetrail.ingestion.parser.records.logs.LogRecordDto;
import com.tracetrail.ingestion.parser.records.logs.OtlpLogsRequest;
import com.tracetrail.ingestion.parser.records.logs.ScopeLog;
import com.tracetrail.ingestion.parser.records.metrics.MetricDto;
import com.tracetrail.ingestion.parser.records.metrics.NumberDataPoint;
import com.tracetrail.ingestion.parser.records.metrics.OtlpMetricsRequest;
import com.tracetrail.ingestion.parser.records.metrics.ScopeMetric;
import com.tracetrail.ingestion.parser.records.parsed.ParsedLogRecords;
import com.tracetrail.ingestion.parser.records.parsed.ParsedMetricPoints;
import com.tracetrail.ingestion.parser.records.parsed.ParsedTraces;
import com.tracetrail.ingestion.parser.records.traces.Attribute;
import com.tracetrail.ingestion.parser.records.traces.OtlpTracesRequest;
import com.tracetrail.ingestion.parser.records.traces.ScopeSpan;
import com.tracetrail.ingestion.parser.records.traces.SpanDto;
import com.tracetrail.ingestion.persistence.models.LogRecord;
import com.tracetrail.ingestion.persistence.models.MetricPoint;
import com.tracetrail.ingestion.persistence.models.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtlpParser {
    private final ObjectMapper objectMapper;

    public ParsedTraces parseTraces(OtlpTracesRequest req,
            long tenantId) {
        List<Span> out = new ArrayList<>();
        int rejected = 0;

        if (req == null || req.resourceSpans() == null) {
            return new ParsedTraces(out, 0, "");
        }

        for (var resourceSpan : req.resourceSpans()) {
            String serviceName = Attributes.findAttribute(
                    resourceSpan.resource() == null ? null : resourceSpan.resource().attributes(),
                    "service.name");

            List<ScopeSpan> scopeSpans = resourceSpan.scopeSpans();

            if (serviceName == null || serviceName.isBlank()) {
                int spanCount = countSpans(scopeSpans);
                rejected += spanCount;
                log.warn("skipping ResourceSpan, missing service.name, spans={}", rejected);
                continue;
            }

            for (ScopeSpan scopeSpan : scopeSpans) {

                for (SpanDto span : scopeSpan.spans()) {
                    long start;
                    long end;

                    try {
                        start = Long.parseLong(span.startTimeUnixNano());
                        end = Long.parseLong(span.endTimeUnixNano());
                    } catch (Exception e) {
                        log.warn("skipped span. could not parse start and end times");
                        rejected++;
                        continue;
                    }
                    long duration = end - start;
                    String parentSpanId = (span.parentSpanId() == null ||
                            span.parentSpanId().isEmpty() ? null : span.parentSpanId());
                    String attrJson;
                    try {
                        attrJson = objectMapper.writeValueAsString(span.attributes());
                    } catch (Exception e) {
                        attrJson = "[]";
                    }

                    out.add(Span.builder()
                            .parentSpanId(parentSpanId)
                            .traceId(span.traceId())
                            .attributesJson(attrJson)
                            .kind((byte) (span.kind() == null
                                    ? 0
                                    : span.kind()))
                            .spanId(span.spanId())
                            .durationNano(duration)
                            .statusCode((byte) (span.status() == null
                                    || span.status().code() == null
                                            ? 0
                                            : span.status().code()))
                            .serviceName(serviceName)
                            .spanName(span.name())
                            .timeUnixNano(start)
                            .endTimeUnixNano(end)
                            .tenantId(tenantId)
                            .build());
                }
            }

        }

        return new ParsedTraces(out, rejected, "");
    }

    public ParsedLogRecords parseLogRecords(OtlpLogsRequest req,
            long tenantId) {
        List<LogRecord> out = new ArrayList<>();
        int rejected = 0;

        if (req == null || req.resourceLogs() == null) {
            return new ParsedLogRecords(out, 0, "");
        }

        for (var resourceLog : req.resourceLogs()) {
            List<Attribute> attributes = resourceLog.resource() == null
                    ? null
                    : resourceLog.resource().attributes();
            String serviceName = Attributes.findAttribute(
                    attributes,
                    "service.name");

            List<ScopeLog> scopeLogs = resourceLog.scopeLogs();

            if (serviceName == null || serviceName.isBlank()) {
                int logCount = countLogs(scopeLogs);
                rejected += logCount;
                log.warn("skipping ResourceLog, missing service.name, log={}", rejected);
                continue;
            }

            for (ScopeLog scopeLog : scopeLogs) {

                for (LogRecordDto logRecordDto : scopeLog.logRecords()) {

                    String eventId = Attributes.findAttribute(
                            logRecordDto.attributes(),
                            "event_id");
                    if (eventId == null || eventId.isBlank()) {
                        eventId = UUID.randomUUID().toString();
                    }
                    long time;

                    try {
                        time = Long.parseLong(logRecordDto.timeUnixNano());
                    } catch (Exception e) {
                        log.warn("skipped log. could not parse time");
                        rejected++;
                        continue;
                    }

                    String attrJson;
                    try {
                        attrJson = objectMapper.writeValueAsString(logRecordDto.attributes());
                    } catch (Exception e) {
                        attrJson = "[]";
                    }
                    String body = logRecordDto.body() == null ? ""
                            : logRecordDto.body().stringValue();

                    out.add(LogRecord.builder()
                            .serviceName(serviceName)
                            .timeUnixNano(time)
                            .spanId(logRecordDto.spanId())
                            .attributesJson(attrJson)
                            .tenantId(tenantId)
                            .severityText(logRecordDto.severityText())
                            .severityNumber((byte) (logRecordDto.severityNumber() == null
                                    ? 0
                                    : logRecordDto.severityNumber()))
                            .traceId(logRecordDto.traceId())
                            .eventId(eventId)
                            .body(body)
                            .build());
                }
            }
        }
        return new ParsedLogRecords(out, rejected, "");
    }

    public ParsedMetricPoints parseMetrics(OtlpMetricsRequest req, long tenantId) {
        List<MetricPoint> out = new ArrayList<>();
        int rejected = 0;

        if (req == null || req.resourceMetrics() == null) {
            return new ParsedMetricPoints(out, 0, "");
        }

        for (var resourceMetric : req.resourceMetrics()) {
            String serviceName = Attributes.findAttribute(
                    resourceMetric.resource() == null ? null
                            : resourceMetric.resource().attributes(),
                    "service.name");

            List<ScopeMetric> scopeMetrics = resourceMetric.scopeMetrics();

            if (serviceName == null || serviceName.isBlank()) {
                int pointCount = countPoints(scopeMetrics);
                rejected += pointCount;
                log.warn("skipping ResourceMetric, missing service.name, points={}", pointCount);
                continue;
            }

            for (ScopeMetric scopeMetric : scopeMetrics) {
                for (MetricDto metric : scopeMetric.metrics()) {

                    // SUM = type 1
                    if (metric.sum() != null && metric.sum().dataPoints() != null) {
                        for (var dp : metric.sum().dataPoints()) {
                            MetricPoint mp = buildNumberPoint(dp, metric.name(),
                                    serviceName, tenantId, (byte) 1);
                            if (mp == null) {
                                rejected++;
                                continue;
                            }
                            out.add(mp);
                        }
                    }
                    // GAUGE = type 2
                    else if (metric.gauge() != null && metric.gauge().dataPoints() != null) {
                        for (var dp : metric.gauge().dataPoints()) {
                            MetricPoint mp = buildNumberPoint(dp, metric.name(),
                                    serviceName, tenantId, (byte) 2);
                            if (mp == null) {
                                rejected++;
                                continue;
                            }
                            out.add(mp);
                        }
                    }
                    // HISTOGRAM = type 3
                    else if (metric.histogram() != null && metric.histogram().dataPoints() != null) {
                        for (var dp : metric.histogram().dataPoints()) {
                            long t;
                            try {
                                t = Long.parseLong(dp.timeUnixNano());
                            } catch (Exception e) {
                                log.warn("skipped histogram point, bad timeUnixNano");
                                rejected++;
                                continue;
                            }
                            String bucketsJson;
                            try {
                                bucketsJson = objectMapper.writeValueAsString(java.util.Map.of(
                                        "bucketCounts", dp.bucketCounts(),
                                        "explicitBounds", dp.explicitBounds()));
                            } catch (Exception e) {
                                bucketsJson = "{}";
                            }

                            String attrJson;
                            try {
                                attrJson = objectMapper.writeValueAsString(dp.attributes());
                            } catch (Exception e) {
                                attrJson = "[]";
                            }

                            out.add(MetricPoint.builder()
                                    .timeUnixNano(t)
                                    .serviceName(serviceName)
                                    .metricName(metric.name())
                                    .attributesHash(hashAttributes(dp.attributes()))
                                    .tenantId(tenantId)
                                    .metricType((byte) 3)
                                    .valueSum(dp.sum())
                                    .valueCount(dp.count())
                                    .bucketsJson(bucketsJson)
                                    .attributesJson(attrJson)
                                    .build());
                        }
                    }
                    // none set → skip whole metric, nothing to count
                }
            }
        }
        return new ParsedMetricPoints(out, rejected, "");
    }

    private MetricPoint buildNumberPoint(NumberDataPoint dp, String metricName,
            String serviceName, long tenantId, byte type) {
        long t;
        try {
            t = Long.parseLong(dp.timeUnixNano());
        } catch (Exception e) {
            log.warn("skipped metric point, bad timeUnixNano");
            return null;
        }

        Double val = dp.asDouble() != null ? dp.asDouble()
                : (dp.asInt() != null ? dp.asInt().doubleValue() : null);

        String attrJson;
        try {
            attrJson = objectMapper.writeValueAsString(dp.attributes());
        } catch (Exception e) {
            attrJson = "[]";
        }

        return MetricPoint.builder()
                .timeUnixNano(t)
                .serviceName(serviceName)
                .metricName(metricName)
                .attributesHash(hashAttributes(dp.attributes()))
                .tenantId(tenantId)
                .metricType(type)
                .valueDouble(val)
                .attributesJson(attrJson)
                .build();
    }

    private String hashAttributes(List<Attribute> attrs) {
        if (attrs == null)
            attrs = List.of();

        // sort keys alphabetically, build k=v|k=v
        String joined = attrs.stream()
                .sorted(java.util.Comparator.comparing(Attribute::key))
                .map(a -> a.key() + "=" + a.value().stringValue())
                .collect(java.util.stream.Collectors.joining("|"));

        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(joined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++)
                sb.append(String.format("%02x", digest[i]));
            return sb.toString(); // 16 hex chars
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 missing", e); // never happens
        }
    }

    private int countSpans(List<ScopeSpan> scopeSpans) {
        int count = 0;
        for (ScopeSpan scopeSpan : scopeSpans) {
            count += scopeSpan.spans().size();
        }
        return count;
    }

    private int countLogs(List<ScopeLog> scopeLogs) {
        int count = 0;
        for (ScopeLog scopeLog : scopeLogs) {
            count += scopeLog.logRecords().size();
        }
        return count;
    }

    private int countPoints(List<ScopeMetric> scopeMetrics) {
        int count = 0;
        for (ScopeMetric sm : scopeMetrics) {
            for (MetricDto m : sm.metrics()) {
                if (m.sum() != null && m.sum().dataPoints() != null)
                    count += m.sum().dataPoints().size();
                else if (m.gauge() != null && m.gauge().dataPoints() != null)
                    count += m.gauge().dataPoints().size();
                else if (m.histogram() != null && m.histogram().dataPoints() != null)
                    count += m.histogram().dataPoints().size();
            }
        }
        return count;
    }
}
