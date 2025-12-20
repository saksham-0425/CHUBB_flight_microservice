package com.booking.bookingservice.util;

import java.util.UUID;

import lombok.Data;
@Data
public class PnrGenerator {

    private PnrGenerator() {
        // prevent object creation
    }

    public static String generate() {
        return "PNR-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}
