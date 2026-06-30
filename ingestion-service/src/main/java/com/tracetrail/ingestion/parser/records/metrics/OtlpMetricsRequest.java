package com.tracetrail.ingestion.parser.records.metrics;

import java.util.List;

public record OtlpMetricsRequest(List<ResourceMetric> resourceMetrics) {
}
