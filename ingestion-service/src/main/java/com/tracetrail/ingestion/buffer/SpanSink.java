package com.tracetrail.ingestion.buffer;

import com.tracetrail.ingestion.persistence.models.Span;

import java.util.List;

public interface SpanSink {
    void accept(long tenantId, List<Span> spans);
}