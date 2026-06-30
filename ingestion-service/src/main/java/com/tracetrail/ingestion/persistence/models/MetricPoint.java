package com.tracetrail.ingestion.persistence.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "metric_points")
@Getter
@IdClass(MetricPointId.class)
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class MetricPoint {

    @Id
    @Column(name = "time_unix_nano")
    private long timeUnixNano;

    @Id
    @Column(name = "service_name")
    private String serviceName;

    @Id
    @Column(name = "metric_name")
    private String metricName;

    @Id
    @Column(name = "attributes_hash")
    private String attributesHash;

    @Column(name = "tenant_id")
    private long tenantId;

    @Column(name = "metric_type")
    private byte metricType;

    @Column(name = "value_double")
    private Double valueDouble;       // nullable → boxed

    @Column(name = "value_sum")
    private Double valueSum;          // nullable → boxed

    @Column(name = "value_count")
    private Long valueCount;          // nullable → boxed

    @Column(name = "buckets_json", columnDefinition = "JSON")
    private String bucketsJson;       // nullable

    @Column(name = "attributes_json", columnDefinition = "JSON")
    private String attributesJson;
}