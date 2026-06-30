package com.tracetrail.ingestion.parser.records.metrics;

import com.tracetrail.ingestion.parser.records.traces.Resource;

import java.util.List;

public record ResourceMetric(Resource resource,
                             List<ScopeMetric> scopeMetrics) {
}
