package com.dailycodework.lakesidehotel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for dynamic pricing calculations
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynamicPricingResponse {
    private Long roomId;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private List<PricingAdjustment> adjustments;
    private BigDecimal totalAdjustmentAmount;
    private String currency = "NPR";
}
