package com.tracetrail.ingestion.parser.records.metrics;

import com.tracetrail.ingestion.parser.records.traces.Attribute;

import java.util.List;

public record HistogramDataPoint(String timeUnixNano,
                                 Double sum,
                                 Long count,
                                 List<Double> explicitBounds,
                                 List<Long> bucketCounts,
                                 List<Attribute> attributes) {
}
