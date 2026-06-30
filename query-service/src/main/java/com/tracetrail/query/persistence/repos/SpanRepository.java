package com.tracetrail.query.persistence.repos;

import com.tracetrail.query.persistence.models.Span;
import com.tracetrail.query.persistence.models.SpanId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpanRepository extends JpaRepository<Span, SpanId> {

    @Query(value = """
    SELECT * FROM spans WHERE tenant_id = :tenantId AND service_name = :service
    AND time_unix_nano BETWEEN :from AND :to ORDER BY time_unix_nano
    DESC LIMIT :limit
""", nativeQuery = true)
     List<Span> findInWindow(@Param("tenantId") long t,
                             @Param("service") String s,
                             @Param("from") long fromNanos,
                             @Param("to") long toNanos,
                             @Param("limit") long l);

    List<Span> findByTenantIdAndTraceIdOrderByTimeUnixNanoAsc
            (long tenantId, String traceId);

    @Query(value = """
    SELECT duration_nano FROM spans WHERE tenant_id = :tenantId AND
    service_name = :service AND time_unix_nano
    BETWEEN :from AND :to ORDER BY duration_nano ASC
""", nativeQuery = true)
    List<Long> findDurations(@Param("tenantId") long t,
                             @Param("service") String s,
                             @Param("from") long fromNanos,
                             @Param("to") long toNanos);
}
