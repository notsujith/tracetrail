package com.tracetrail.ingestion.buffer;

import com.tracetrail.ingestion.persistence.models.Span;

public record SpanEnvelope(
        long timeUnixNano,
        String traceId,
        String spanId,
        String parentSpanId,
        long tenantId,
        String serviceName,
        String spanName,
        byte kind,
        long endTimeUnixNano,
        long durationNano,
        byte statusCode,
        String attributesJson
) {
    public static SpanEnvelope from(Span s) {
        return new SpanEnvelope(
                s.getTimeUnixNano(),
                s.getTraceId(),
                s.getSpanId(),
                s.getParentSpanId(),
                s.getTenantId(),
                s.getServiceName(),
                s.getSpanName(),
                s.getKind(),
                s.getEndTimeUnixNano(),
                s.getDurationNano(),
                s.getStatusCode(),
                s.getAttributesJson()
        );
    }

    public Span toEntity() {
        return Span.builder()
                .timeUnixNano(timeUnixNano)
                .traceId(traceId)
                .spanId(spanId)
                .parentSpanId(parentSpanId)
                .tenantId(tenantId)
                .serviceName(serviceName)
                .spanName(spanName)
                .kind(kind)
                .endTimeUnixNano(endTimeUnixNano)
                .durationNano(durationNano)
                .statusCode(statusCode)
                .attributesJson(attributesJson)
                .build();
    }
}