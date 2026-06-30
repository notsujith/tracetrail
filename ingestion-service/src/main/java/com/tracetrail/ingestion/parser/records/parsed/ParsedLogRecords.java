package com.tracetrail.ingestion.parser.records.parsed;

import com.tracetrail.ingestion.persistence.models.LogRecord;

import java.util.List;

public record ParsedLogRecords(List<LogRecord> records,
                               int rejected,
                               String errorMessage) {
}
