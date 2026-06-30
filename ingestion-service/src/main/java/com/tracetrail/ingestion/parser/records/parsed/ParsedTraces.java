package com.tracetrail.ingestion.parser.records.parsed;

import com.tracetrail.ingestion.persistence.models.Span;

import java.util.List;

public record ParsedTraces(List<Span> spans,
                           int rejected,
                           String errorMessage) {
}
