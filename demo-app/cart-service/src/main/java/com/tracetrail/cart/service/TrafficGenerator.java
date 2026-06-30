package com.tracetrail.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrafficGenerator {

    private final AtomicLong ticks = new AtomicLong();
    private final RestClient http;

    @Scheduled(fixedRate = 1000)
    public void tick() {
        try {
            String userId = UUID.randomUUID().toString();
            Map<String, Object> cart = http.get()
                    .uri("http://localhost:8080/carts/{userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (ticks.incrementAndGet() % 5 == 0) {
                http.post()
                        .uri("http://localhost:8080/carts/{userId}/checkout",
                                userId)
                        .body(cart)
                        .retrieve()
                        .toBodilessEntity();

            }
        } catch (Exception e) {
            log.error("Error while generating traffic :{}", e.getMessage());
        }
    }
}
