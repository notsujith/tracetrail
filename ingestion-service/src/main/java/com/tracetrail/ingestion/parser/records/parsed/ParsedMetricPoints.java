package com.tracetrail.ingestion.parser.records.parsed;

import com.tracetrail.ingestion.persistence.models.MetricPoint;

import java.util.List;

public record ParsedMetricPoints(List<MetricPoint> points,
                                 int rejected,
                                 String errorMessage) {
}
