package com.tracetrail.ingestion.buffer;

import com.tracetrail.ingestion.persistence.models.Span;
import com.tracetrail.ingestion.persistence.repos.ServiceRepository;
import com.tracetrail.ingestion.persistence.repos.SpanRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tracetrail.ingestion.buffer", havingValue = "kafka")
public class SpanConsumer {

    private static final Logger log = LoggerFactory.getLogger(SpanConsumer.class);

    private final SpanRepository spanRepo;
    private final ServiceRepository serviceRepo;

    @KafkaListener(
            topics = "${tracetrail.ingestion.kafka.topic:tt.spans}",
            groupId = "${spring.kafka.consumer.group-id:tt-ingestion}"
    )
    public void drain(List<SpanEnvelope> batch) {
        if (batch.isEmpty()) {
            return;
        }
        List<Span> spans = batch.stream().map(SpanEnvelope::toEntity).toList();
        spanRepo.saveAll(spans);

        Map<Long, Set<String>> servicesByTenant = new HashMap<>();
        for (Span s : spans) {
            servicesByTenant
                    .computeIfAbsent(s.getTenantId(), k -> new HashSet<>())
                    .add(s.getServiceName());
        }
        servicesByTenant.forEach((tenantId, names) ->
                names.forEach(name -> serviceRepo.upsert(tenantId, name)));

        log.debug("drained {} spans across {} tenant(s)", spans.size(), servicesByTenant.size());
    }
}