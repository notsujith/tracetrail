package com.tracetrail.ingestion.persistence.repos;

import com.tracetrail.ingestion.persistence.models.MetricPoint;
import com.tracetrail.ingestion.persistence.models.MetricPointId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricRepository extends JpaRepository<MetricPoint, MetricPointId> {
}
