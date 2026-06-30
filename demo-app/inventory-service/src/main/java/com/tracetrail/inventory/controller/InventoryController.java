package com.tracetrail.inventory.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class InventoryController {

    @GetMapping("/inventory/{itemId}")
    public Map<String, Object> checkStock(@PathVariable String itemId) {
        return Map.of("itemId", itemId,
                "stock", ThreadLocalRandom.current().nextInt(0, 100));
    }

    @PostMapping("/inventory/{itemId}/reserve")
    public Map<String, Object> reserve(@PathVariable String itemId) {
        return Map.of("reservationId", UUID.randomUUID().toString(),
                "itemId", itemId);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
