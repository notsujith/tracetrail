package com.tracetrail.ingestion.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {
    @Bean
    public CacheManager cacheManager(){
        Caffeine<Object, Object> caffeine =
                Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .maximumSize(10_000);
        CaffeineCacheManager cacheManager =
                new CaffeineCacheManager("tenant-by-key");
        cacheManager.setCaffeine(caffeine);
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }
}
