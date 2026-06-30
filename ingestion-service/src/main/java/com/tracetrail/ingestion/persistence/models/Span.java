package com.tracetrail.ingestion.persistence.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "spans")
@Getter
@IdClass(SpanId.class)
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Span {
    @Id
    @Column(name = "time_unix_nano")
    private long timeUnixNano;

    @Id
    @Column(name = "trace_id")
    private String traceId;

    @Id
    @Column(name = "span_id")
    private String spanId;

    @Column(name = "parent_span_id")
    private String parentSpanId;

    private long tenantId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "span_name")
    private String spanName;

    private byte kind;

    @Column(name = "end_time_unix_nano")
    private long endTimeUnixNano;

    @Column(name = "duration_nano")
    private long durationNano;

    private byte statusCode;

    @Column(name = "attributes_json", columnDefinition = "JSON")
    private String attributesJson;


}
