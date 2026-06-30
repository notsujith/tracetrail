package com.tracetrail.ingestion.controller.otlp;

import com.tracetrail.ingestion.parser.otlp.OtlpParser;
import com.tracetrail.ingestion.parser.records.logs.OtlpLogsRequest;
import com.tracetrail.ingestion.parser.records.metrics.OtlpMetricsRequest;
import com.tracetrail.ingestion.parser.records.parsed.ParsedLogRecords;
import com.tracetrail.ingestion.parser.records.parsed.ParsedMetricPoints;
import com.tracetrail.ingestion.parser.records.parsed.ParsedTraces;
import com.tracetrail.ingestion.parser.records.traces.OtlpTracesRequest;
import com.tracetrail.ingestion.persistence.models.LogRecord;
import com.tracetrail.ingestion.persistence.models.MetricPoint;
import com.tracetrail.ingestion.persistence.models.Span;
import com.tracetrail.ingestion.persistence.models.Tenant;
import com.tracetrail.ingestion.persistence.repos.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class  OtlpController {

    private final OtlpParser parser;
    private final SpanRepository spanRepo;
    private final MetricRepository metricRepo;
    private final LogRecordRepository logRecordRepo;
    private final ServiceRepository serviceRepo;
    private final TenantRepository tenantRepository;

    @PostMapping(value = "/v1/traces", consumes = "application/json")
    public ResponseEntity<?> ingestTraces(
            @RequestBody @Valid OtlpTracesRequest body,
            HttpServletRequest req,
            HttpServletResponse res

    ) {
        Tenant tenant = (Tenant) req.getAttribute("tenant");
        ParsedTraces parsedTraces = parser.parseTraces(body, tenant.getId());
        try {
            spanRepo.saveAll(parsedTraces.spans());
        } catch (DataAccessException e){
            return ResponseEntity.status(503).body(
                    Map.of("error", "database unreachable"));
        }

        parsedTraces.spans()
                .stream()
                .map(Span::getServiceName)
                .collect(Collectors.toSet())
                .forEach(serviceName ->
                        serviceRepo.upsert(tenant.getId(),
                                serviceName));
        var returnBody = Map.of("partialSuccess",
                Map.of("rejectedSpans", parsedTraces.rejected(),
                        "error message", parsedTraces.errorMessage())
        );

        return ResponseEntity.ok().body(returnBody);
    }


    @PostMapping(value = "/v1/metrics", consumes = "application/json")
    public ResponseEntity<?> ingestMetrics(
            @RequestBody @Valid OtlpMetricsRequest body,
            HttpServletRequest req,
            HttpServletResponse res
            ) {

        Tenant tenant = (Tenant) req.getAttribute("tenant");
        ParsedMetricPoints parsedMetrics = parser
                .parseMetrics(body, tenant.getId());

        try {
            metricRepo.saveAll(parsedMetrics.points());
        } catch (DataAccessException e){
            return ResponseEntity.status(503).body(
                    Map.of("error", "database unreachable"));
        }

        parsedMetrics.points()
                .stream()
                .map(MetricPoint::getServiceName)
                .collect(Collectors.toSet())
                .forEach(serviceName ->
                        serviceRepo.upsert(tenant.getId(),
                                serviceName));

        var returnBody = Map.of("partialSuccess",
                Map.of("rejectedMetrics", parsedMetrics.rejected(),
                        "error message", parsedMetrics.errorMessage())
        );


        return ResponseEntity.ok().body(returnBody);
    }


    @PostMapping(value = "/v1/logs", consumes = "application/json")
    public ResponseEntity<?> ingestLogs(
            @RequestBody @Valid OtlpLogsRequest body,
            HttpServletRequest req,
            HttpServletResponse res
            ) {

        Tenant tenant = (Tenant) req.getAttribute("tenant");
        ParsedLogRecords parsedLogRecords = parser
                .parseLogRecords(body, tenant.getId());

        try {
            logRecordRepo.saveAll(parsedLogRecords.records());
        } catch (DataAccessException e){
            return ResponseEntity.status(503).body(
                    Map.of("error", "database unreachable"));
        }

        parsedLogRecords.records()
                .stream()
                .map(LogRecord::getServiceName)
                .collect(Collectors.toSet())
                .forEach(serviceName ->
                        serviceRepo.upsert(tenant.getId(),
                                serviceName));

        var returnBody = Map.of("partialSuccess",
                Map.of("rejectedLogRecords", parsedLogRecords.rejected(),
                        "error message", parsedLogRecords.errorMessage())
        );
        return ResponseEntity.ok().body(returnBody);
    }

}
