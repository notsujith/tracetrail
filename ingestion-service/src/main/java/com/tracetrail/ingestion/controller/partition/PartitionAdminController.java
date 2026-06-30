package com.tracetrail.ingestion.controller.partition;

import com.tracetrail.ingestion.scheduler.PartitionScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PartitionAdminController {

    private final PartitionScheduler scheduler;

    @Value("${tracetrail.admin-token}")
    private String adminToken;

    @PostMapping("/admin/partitions/run")
    public ResponseEntity<?> run(
            @RequestHeader(value = "X-Admin-Token", required = false)
                                 String token){
        if (token == null || !token.equals(adminToken)){
            return ResponseEntity.status(401)
                    .body(Map.of("errorOf", "invalid admin token"));
        }

        scheduler.runMaintenance();
        return ResponseEntity.ok()
                .body(Map.of("status", "ok"));
    }
}
