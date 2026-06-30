package com.tracetrail.ingestion.persistence.models;

import java.io.Serializable;
import java.util.Objects;

public class LogRecordId implements Serializable {

    private long timeUnixNano;
    private String serviceName;
    private String eventId;

    public LogRecordId() {}

    public LogRecordId(long timeUnixNano, String serviceName, String eventId) {
        this.timeUnixNano = timeUnixNano;
        this.serviceName = serviceName;
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LogRecordId other)) return false;
        return timeUnixNano == other.timeUnixNano
                && Objects.equals(serviceName, other.serviceName)
                && Objects.equals(eventId, other.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeUnixNano, serviceName, eventId);
    }
}