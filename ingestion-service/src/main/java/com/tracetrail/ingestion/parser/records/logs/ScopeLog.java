package com.tracetrail.ingestion.parser.records.logs;

import java.util.List;

public record ScopeLog(List<LogRecordDto> logRecords) {
}
