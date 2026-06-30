package com.tracetrail.ingestion.parser.records.logs;

import java.util.List;

public record OtlpLogsRequest(List<ResourceLog> resourceLogs) {
}
