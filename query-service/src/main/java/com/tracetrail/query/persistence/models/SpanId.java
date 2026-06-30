package com.tracetrail.query.persistence.models;

import java.io.Serializable;
import java.util.Objects;

public class SpanId implements Serializable {

    private long timeUnixNano;
    private String traceId;
    private String spanId;

    public SpanId() {}  // JPA needs no-arg ctor

    public SpanId(long timeUnixNano, String traceId, String spanId) {
        this.timeUnixNano = timeUnixNano;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpanId other)) return false;
        return timeUnixNano == other.timeUnixNano
                && Objects.equals(traceId, other.traceId)
                && Objects.equals(spanId, other.spanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeUnixNano, traceId, spanId);
    }
}