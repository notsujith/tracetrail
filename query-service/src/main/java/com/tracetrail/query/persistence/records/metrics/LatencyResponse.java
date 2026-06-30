package com.tracetrail.query.persistence.records.metrics;

public record LatencyResponse(double p50,
                              double p95,
                              double p99,
                              long count,
                              int windowMinutes) {
}
