package com.tracetrail.ingestion.parser.records.logs;

import com.tracetrail.ingestion.parser.records.traces.AnyValue;
import com.tracetrail.ingestion.parser.records.traces.Attribute;

import java.util.List;

public record LogRecordDto(String timeUnixNano,
                           Integer severityNumber,
                           String severityText,
                           AnyValue body,
                           String traceId,
                           String spanId,
                           List<Attribute> attributes) {
}
