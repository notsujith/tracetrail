package com.tracetrail.query.auth;

import com.tracetrail.query.persistence.models.Tenant;
import com.tracetrail.query.persistence.repos.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantResolver {
    private final TenantRepository tenantRepository;

    @Cacheable(value = "tenant-by-key", key = "#apiKey")
    public Tenant resolveByAPIKey(String apiKey){
        return tenantRepository
                .findByApiKey(apiKey)
                .orElse(null);
    }
}
