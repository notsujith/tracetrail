package com.tracetrail.ingestion.persistence.repos;

import com.tracetrail.ingestion.persistence.models.Span;
import com.tracetrail.ingestion.persistence.models.SpanId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpanRepository extends JpaRepository<Span, SpanId> {
}
