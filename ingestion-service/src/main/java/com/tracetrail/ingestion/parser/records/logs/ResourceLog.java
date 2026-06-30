package com.tracetrail.ingestion.parser.records.logs;

import com.tracetrail.ingestion.parser.records.traces.Resource;

import java.util.List;

public record ResourceLog(Resource resource,
                          List<ScopeLog> scopeLogs) {
}
