package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.response.DynamicPricingResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service interface for dynamic pricing calculations
 */
public interface IDynamicPricingService {

    /**
     * Calculate dynamic pricing for a room booking
     * 
     * @param roomId   The ID of the room
     * @param checkIn  Check-in date
     * @param checkOut Check-out date
     * @return DynamicPricingResponse containing final price and adjustments
     */
    DynamicPricingResponse calculatePrice(Long roomId, LocalDate checkIn, LocalDate checkOut);

    /**
     * Calculate dynamic pricing for a specific date range
     * 
     * @param roomId    The ID of the room
     * @param checkIn   Check-in date
     * @param checkOut  Check-out date
     * @param basePrice Base price to use for calculations
     * @return DynamicPricingResponse containing final price and adjustments
     */
    DynamicPricingResponse calculatePrice(Long roomId, LocalDate checkIn, LocalDate checkOut, BigDecimal basePrice);

    /**
     * Apply timing-based adjustments (last-minute, early-bird) to a pricing
     * response
     * 
     * @param response The base pricing response
     * @param checkIn  Check-in date
     * @return Updated pricing response with timing adjustments
     */
    DynamicPricingResponse applyTimingAdjustments(DynamicPricingResponse response, LocalDate checkIn);

    /**
     * Calculate last-minute booking adjustment percentage
     * 
     * @param checkIn Check-in date
     * @return Adjustment percentage (0.25 for last-minute, 0 for normal)
     */
    BigDecimal calculateLastMinuteAdjustment(LocalDate checkIn);

    /**
     * Calculate early-bird discount percentage
     * 
     * @param checkIn Check-in date
     * @return Discount percentage (0.10 for early-bird, 0 for normal)
     */
    BigDecimal calculateEarlyBirdDiscount(LocalDate checkIn);
}
