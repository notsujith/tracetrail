package com.tracetrail.payment.models;

public record Payment(int amount,
                      String currency,
                      String userId) {
}
