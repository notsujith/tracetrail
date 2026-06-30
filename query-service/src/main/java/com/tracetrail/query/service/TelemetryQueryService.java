package com.tracetrail.query.service;

import com.tracetrail.query.persistence.models.Span;
import com.tracetrail.query.persistence.records.metrics.LatencyResponse;
import com.tracetrail.query.persistence.repos.ServiceRepository;
import com.tracetrail.query.persistence.repos.SpanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelemetryQueryService {
    private final ServiceRepository serviceRepo;
    private final SpanRepository spanRepo;
    private final ObjectMapper objectMapper;


    @Cacheable(value = "services-list", key = "#tenantId + ':' + #lookBackMinutes")
    public List<ServiceRepository.ServiceSummary> listServices(long tenantId,
                                                               int lookbackMinutes){
        long lookBackNanos = lookbackMinutes * 60 * 1_000_000_000L;
        return serviceRepo.getServices(lookBackNanos, tenantId);
    }

    @Cacheable(value="service-spans",
            key="#tenantId + ':' + #service + ':' + " +
                    "#from + ':' + #to + ':' + #limit")
    public List<Span> getSpans(long tenantId, String service, long from,
                                  long to, int limit){
        return spanRepo.findInWindow(tenantId, service, from, to, limit);

    }


    @Cacheable(value = "service-latency", key = "#tenantId + ':' + " +
            "#service + ':' + #windowMinutes")
    public LatencyResponse getLatency(long tenantId, String service,
                                      int windowMinutes){
        long toNanos = System.currentTimeMillis() * 1_000_000L;
        long fromNanos = toNanos - (long) windowMinutes * 60L * 1_000_000_000L;

        List<Long> durations = spanRepo.findDurations(tenantId, service, fromNanos, toNanos);

        if (durations.isEmpty()) {
            return new LatencyResponse(0.0, 0.0, 0.0, 0, windowMinutes);
        }

        int n = durations.size();
        int i50 = Math.min((int) (n * 0.50), n - 1);
        int i95 = Math.min((int) (n * 0.95), n - 1);
        int i99 = Math.min((int) (n * 0.99), n - 1);

        double p50 = durations.get(i50) / 1_000_000.0;
        double p95 = durations.get(i95) / 1_000_000.0;
        double p99 = durations.get(i99) / 1_000_000.0;

        return new LatencyResponse(p50, p95, p99, n, windowMinutes);
    }
}
