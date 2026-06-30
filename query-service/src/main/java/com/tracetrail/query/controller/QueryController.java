package com.tracetrail.query.controller;

import com.tracetrail.query.persistence.models.Span;
import com.tracetrail.query.persistence.models.Tenant;
import com.tracetrail.query.persistence.records.metrics.LatencyResponse;
import com.tracetrail.query.persistence.repos.ServiceRepository;
import com.tracetrail.query.persistence.repos.SpanRepository;
import com.tracetrail.query.service.TelemetryQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QueryController {
        private final TelemetryQueryService service;
        private final SpanRepository spanRepo;

        @GetMapping("/services")
        public ResponseEntity<?> getServices(@RequestParam(defaultValue = "60") int lookbackMinutes,
                        HttpServletRequest req) {
                Tenant tenant = (Tenant) req.getAttribute("tenant");
                List<ServiceRepository.ServiceSummary> services = service.listServices(tenant.getId(), lookbackMinutes);

                return ResponseEntity.ok().body(services);
        }

        @GetMapping("/services/{name}/spans")
        public ResponseEntity<?> spans(@PathVariable String name,
                        @RequestParam long from,
                        @RequestParam long to,
                        @RequestParam(defaultValue = "100") int limit,
                        HttpServletRequest req) {
                Tenant tenant = (Tenant) req.getAttribute("tenant");
                if (to - from >= 3_600_000) {
                        return ResponseEntity.status(400)
                                        .body("time window too large; max 1 hour");
                }
                long fromNanos = from * 1_000_000L;
                long toNanos = to * 1_000_000L;
                List<Span> body = service.getSpans(
                                tenant.getId(),
                                name,
                                fromNanos,
                                toNanos,
                                limit);
                return ResponseEntity.ok().body(body);
        }

        @GetMapping("/traces/{traceId}")
        public ResponseEntity<?> traces(@PathVariable String traceId,
                        HttpServletRequest req) {
                Tenant tenant = (Tenant) req.getAttribute("tenant");
                List<Span> spans = spanRepo
                                .findByTenantIdAndTraceIdOrderByTimeUnixNanoAsc(
                                                tenant.getId(),
                                                traceId);

                if (spans == null || spans.isEmpty()) {
                        return ResponseEntity.status(404)
                                        .body("trace not found in retention window");
                }
                return ResponseEntity.ok()
                                .body(spans);
        }

        @GetMapping("services/{name}/latency")
        public ResponseEntity<?> latency(@PathVariable String name,
                        @RequestParam(defaultValue = "5") int windowMinutes,
                        HttpServletRequest req) {
                Tenant tenant = (Tenant) req.getAttribute("tenant");
                LatencyResponse latency = service.getLatency(tenant.getId(), name, windowMinutes);
                return ResponseEntity.ok().body(latency);

        }

}
