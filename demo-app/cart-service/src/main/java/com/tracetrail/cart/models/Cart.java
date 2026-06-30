package com.tracetrail.cart.models;

import java.util.List;
import java.util.Map;

public record Cart(String userId,
                   List<Map<String, Object>> items,
                   int itemsCount) {
}
