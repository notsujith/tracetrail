package com.tracetrail.payment.controller;

import com.tracetrail.payment.models.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    @PostMapping("/payments")
    public Map<String, Object> payments(@RequestBody Payment payment){
        if (ThreadLocalRandom.current().nextInt(100) < 10){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Payment gateway declined"
            );
        }
        return Map.of(
                "paymentId", UUID.randomUUID().toString(),
                "status", "payment captured",
                "amount", payment.amount(),
                "currency", payment.currency(),
                "userId", payment.userId()
        );
    }

    @GetMapping("/health")
    public ResponseEntity<?> health(){
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
