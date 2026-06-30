package com.tracetrail.ingestion.parser.records.metrics;

import com.tracetrail.ingestion.parser.records.traces.Attribute;

import java.util.List;

public record NumberDataPoint(String timeUnixNano,
                              Double asDouble,
                              Long asInt,
                              List<Attribute> attributes) {
}
