package com.tracetrail.ingestion.parser.records.metrics;

import java.util.List;

public record Sum(List<NumberDataPoint> dataPoints) {
}
