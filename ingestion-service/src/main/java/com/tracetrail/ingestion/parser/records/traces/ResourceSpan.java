package com.tracetrail.ingestion.parser.records.traces;

import java.util.List;

public record ResourceSpan(Resource resource,
                           List<ScopeSpan> scopeSpans) {
}
