package com.dailycodework.lakesidehotel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a single pricing adjustment applied to a room booking
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PricingAdjustment {
    private String name;
    private BigDecimal percentage;
    private BigDecimal amount;
    private String description;
}
