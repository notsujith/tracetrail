package com.tracetrail.query.persistence.repos;

import com.tracetrail.query.persistence.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    interface ServiceSummary{
        String getName();
        LocalDateTime getLastSeen();
        long getSpanCount();
    }

    @Query(value = """
    SELECT svc.service_name AS name, svc.last_seen as lastSeen, COUNT(s.span_id)
    AS spanCount FROM services svc JOIN spans s ON svc.tenant_id = s.tenant_id
    AND svc.service_name = s.service_name AND s.time_unix_nano >= :lookBackNanos
    WHERE svc.tenant_id = :tenantId GROUP BY svc.service_name, svc.last_seen
    ORDER BY spanCount DESC;
""", nativeQuery = true)
    List<ServiceSummary> getServices(long lookBackNanos, long tenantId);

}
