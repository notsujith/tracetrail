package com.tracetrail.ingestion.persistence.repos;

import com.tracetrail.ingestion.persistence.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO services (tenant_id, service_name, first_seen, last_seen)
            VALUES (:tenantId, :serviceName, NOW(), NOW())
            ON DUPLICATE KEY UPDATE last_seen = NOW()
            """, nativeQuery = true)
    void upsert(@Param("tenantId") long tenantId,
            @Param("serviceName") String serviceName);
}