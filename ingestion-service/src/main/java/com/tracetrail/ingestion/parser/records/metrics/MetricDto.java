package com.tracetrail.ingestion.parser.records.metrics;

public record MetricDto(String name,
                        Sum sum,
                        Gauge gauge,
                        Histogram histogram) {
}
