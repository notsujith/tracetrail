package com.tracetrail.ingestion.parser.records.traces;

import java.util.List;

public record ScopeSpan(List<SpanDto> spans) {
}
