package com.tracetrail.query.persistence.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@RequiredArgsConstructor
@Getter
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "rate_limit_capacity")
    private int rateLimitCapacity;

    @Column(name = "rate_limit_refill_per_sec")
    private int rateLimitRefillPerSec;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
