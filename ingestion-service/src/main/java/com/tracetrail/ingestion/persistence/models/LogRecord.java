package com.tracetrail.ingestion.persistence.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "logs")
@Getter
@IdClass(LogRecordId.class)
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class LogRecord {

    @Id
    @Column(name = "time_unix_nano")
    private long timeUnixNano;

    @Id
    @Column(name = "service_name")
    private String serviceName;

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "tenant_id")
    private long tenantId;

    @Column(name = "severity_number")
    private byte severityNumber;

    @Column(name = "severity_text")
    private String severityText;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "trace_id")
    private String traceId;          // nullable

    @Column(name = "span_id")
    private String spanId;           // nullable

    @Column(name = "attributes_json", columnDefinition = "JSON")
    private String attributesJson;
}