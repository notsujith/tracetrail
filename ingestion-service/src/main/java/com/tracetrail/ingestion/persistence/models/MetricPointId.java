package com.tracetrail.ingestion.persistence.models;

import java.io.Serializable;
import java.util.Objects;

public class MetricPointId implements Serializable {
    private long timeUnixNano;
    private String serviceName;
    private String metricName;
    private String attributesHash;

    public MetricPointId(long timeUnixNano, String serviceName, String metricName, String attributesHash) {
        this.timeUnixNano = timeUnixNano;
        this.serviceName = serviceName;
        this.metricName = metricName;
        this.attributesHash = attributesHash;
    }

    public MetricPointId() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeUnixNano, serviceName, metricName, attributesHash);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MetricPointId other)) return false;
        return timeUnixNano == other.timeUnixNano
                && Objects.equals(serviceName, other.serviceName)
                && Objects.equals(metricName, other.metricName)
                && Objects.equals(attributesHash, other.attributesHash);
    }
}
