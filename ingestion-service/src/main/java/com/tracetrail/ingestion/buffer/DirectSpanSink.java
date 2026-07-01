package com.tracetrail.ingestion.buffer;

import com.tracetrail.ingestion.persistence.models.Span;
import com.tracetrail.ingestion.persistence.repos.ServiceRepository;
import com.tracetrail.ingestion.persistence.repos.SpanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tracetrail.ingestion.buffer", havingValue = "direct", matchIfMissing = true)
public class DirectSpanSink implements SpanSink {

    private final SpanRepository spanRepo;
    private final ServiceRepository serviceRepo;

    @Override
    public void accept(long tenantId, List<Span> spans) {
        spanRepo.saveAll(spans);
        spans.stream()
                .map(Span::getServiceName)
                .collect(Collectors.toSet())
                .forEach(serviceName -> serviceRepo.upsert(tenantId, serviceName));
    }
}