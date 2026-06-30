package com.tracetrail.ingestion.parser.records.metrics;

import java.util.List;

public record ScopeMetric(List<MetricDto> metrics) {
}
