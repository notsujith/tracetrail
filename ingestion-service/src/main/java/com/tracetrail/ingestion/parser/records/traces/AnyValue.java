package com.tracetrail.ingestion.parser.records.traces;

public record AnyValue(String stringValue,
                       Long intValue,
                       Boolean boolValue,
                       Double doubleValue) {
}
