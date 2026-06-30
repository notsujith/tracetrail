package com.tracetrail.ingestion.parser.records.traces;

import java.util.List;

public record SpanDto(String traceId,
                      String spanId,
                      String parentSpanId,
                      String name,
                      Integer kind,
                      String startTimeUnixNano,
                      String endTimeUnixNano,
                      Status status,
                      List<Attribute> attributes) {
}
