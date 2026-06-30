package com.tracetrail.cart.controller;

import com.tracetrail.cart.models.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
public class CartController {

    private static final List<String> CATALOG = List.of("sku-1", "sku-2", "sku-3", "sku-4", "sku-5", "sku-6", "sku-7",
            "sku-8");

    @Value("${INVENTORY_URL:http://inventory-service:8080}")
    private String inventoryUrl;

    @Value("${PAYMENT_URL:http://payment-service:8080}")
    private String paymentUrl;

    private final RestClient http;

    @GetMapping("/carts/{userId}")
    public Map<String, Object> getCart(@PathVariable String userId) {

        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String itemId = CATALOG.get(ThreadLocalRandom.current().nextInt(CATALOG.size()));
            Map<String, Object> item = http
                    .get()
                    .uri(inventoryUrl + "/inventory/" + itemId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            items.add(item);
        }

        return Map.of("userId", userId,
                "items", items,
                "itemCount", items.size());

    }

    @PostMapping("/carts/{userId}/checkout")
    public Map<String, Object> checkout(@PathVariable String userId,
            @RequestBody Cart cart) {
        for (var item : cart.items()) {
            http.post().uri(inventoryUrl + "/" + item.get("itemId") + "/reserve");
        }

        try {
            return http.post()
                    .uri(paymentUrl + "/payments")
                    .body(Map.of("amount", ThreadLocalRandom
                            .current().nextInt(100, 300),
                            "currency", "USD",
                            "userId", userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (ResponseStatusException e) {
            {
                return Map.of("userId", "u1",
                        "status", "failed",
                        "stage", "payment",
                        "error", "500 payment gateway declined");
            }
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(Map.of("status", "UP"));
    }
}
