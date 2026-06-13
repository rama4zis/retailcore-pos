package com.retailcore.pos.sale;

import java.time.Instant;
import java.util.UUID;

final class SaleNumberGenerator {

    private SaleNumberGenerator() {
    }

    static String next() {
        return "SALE-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
