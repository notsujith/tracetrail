package com.tracetrail.ingestion.persistence.repos;

import com.tracetrail.ingestion.persistence.models.LogRecord;
import com.tracetrail.ingestion.persistence.models.LogRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRecordRepository extends JpaRepository<LogRecord, LogRecordId> {
}
