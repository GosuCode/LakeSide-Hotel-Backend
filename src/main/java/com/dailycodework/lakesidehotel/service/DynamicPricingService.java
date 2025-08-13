package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.repository.RoomRepository;
import com.dailycodework.lakesidehotel.response.DynamicPricingResponse;
import com.dailycodework.lakesidehotel.response.PricingAdjustment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for dynamic pricing calculations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicPricingService implements IDynamicPricingService {

    private final RoomRepository roomRepository;

    // Pricing constants
    private static final BigDecimal WEEKEND_SURCHARGE_PERCENTAGE = new BigDecimal("0.15");
    private static final BigDecimal SEASONAL_MULTIPLIER_PERCENTAGE = new BigDecimal("0.20");
    private static final BigDecimal LAST_MINUTE_BOOKING_PERCENTAGE = new BigDecimal("0.25");
    private static final BigDecimal EARLY_BIRD_DISCOUNT_PERCENTAGE = new BigDecimal("0.10");

    // Date constants
    private static final int LAST_MINUTE_THRESHOLD_DAYS = 3;
    private static final int EARLY_BIRD_THRESHOLD_DAYS = 30;
    private static final int SEASONAL_START_DAY = 15;
    private static final int SEASONAL_END_DAY = 5;

    @Override
    public DynamicPricingResponse calculatePrice(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        return calculatePrice(roomId, checkIn, checkOut, room.getRoomPrice());
    }

    @Override
    public DynamicPricingResponse calculatePrice(Long roomId, LocalDate checkIn, LocalDate checkOut,
            BigDecimal basePrice) {
        log.info("Calculating dynamic pricing for room {} from {} to {}", roomId, checkIn, checkOut);

        List<PricingAdjustment> adjustments = new ArrayList<>();
        BigDecimal finalPrice = basePrice;

        // Calculate adjustments for each night
        LocalDate currentDate = checkIn;
        while (currentDate.isBefore(checkOut)) {
            List<PricingAdjustment> nightlyAdjustments = calculateNightlyAdjustments(currentDate, basePrice);
            adjustments.addAll(nightlyAdjustments);

            // Apply adjustments to final price
            for (PricingAdjustment adjustment : nightlyAdjustments) {
                finalPrice = finalPrice.add(adjustment.getAmount());
            }

            currentDate = currentDate.plusDays(1);
        }

        // Calculate total adjustment amount
        BigDecimal totalAdjustmentAmount = finalPrice.subtract(basePrice);

        // Round final price to 2 decimal places
        finalPrice = finalPrice.setScale(2, RoundingMode.HALF_UP);
        totalAdjustmentAmount = totalAdjustmentAmount.setScale(2, RoundingMode.HALF_UP);

        log.info("Final price calculated: {} (base: {}, adjustments: {})", finalPrice, basePrice,
                totalAdjustmentAmount);

        return DynamicPricingResponse.builder()
                .roomId(roomId)
                .basePrice(basePrice)
                .finalPrice(finalPrice)
                .adjustments(adjustments)
                .totalAdjustmentAmount(totalAdjustmentAmount)
                .build();
    }

    /**
     * Calculate pricing adjustments for a single night
     */
    private List<PricingAdjustment> calculateNightlyAdjustments(LocalDate date, BigDecimal basePrice) {
        List<PricingAdjustment> adjustments = new ArrayList<>();

        // Weekend surcharge (Friday and Saturday)
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            BigDecimal amount = basePrice.multiply(WEEKEND_SURCHARGE_PERCENTAGE);
            adjustments.add(new PricingAdjustment(
                    "Weekend Surcharge",
                    WEEKEND_SURCHARGE_PERCENTAGE.multiply(new BigDecimal("100")),
                    amount,
                    "15% surcharge for weekend stays"));
        }

        // Seasonal multiplier (Dec 15 - Jan 5)
        if (isSeasonalDate(date)) {
            BigDecimal amount = basePrice.multiply(SEASONAL_MULTIPLIER_PERCENTAGE);
            adjustments.add(new PricingAdjustment(
                    "Seasonal Multiplier",
                    SEASONAL_MULTIPLIER_PERCENTAGE.multiply(new BigDecimal("100")),
                    amount,
                    "20% increase for peak holiday season"));
        }

        return adjustments;
    }

    /**
     * Check if a date falls within the seasonal period
     */
    private boolean isSeasonalDate(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // December 15-31
        if (month == 12 && day >= SEASONAL_START_DAY) {
            return true;
        }

        // January 1-5
        if (month == 1 && day <= SEASONAL_END_DAY) {
            return true;
        }

        return false;
    }

    /**
     * Calculate last-minute booking adjustment
     */
    public BigDecimal calculateLastMinuteAdjustment(LocalDate checkIn) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);

        if (daysUntilCheckIn <= LAST_MINUTE_THRESHOLD_DAYS && daysUntilCheckIn >= 0) {
            return LAST_MINUTE_BOOKING_PERCENTAGE;
        }

        return BigDecimal.ZERO;
    }

    /**
     * Calculate early-bird discount
     */
    public BigDecimal calculateEarlyBirdDiscount(LocalDate checkIn) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);

        if (daysUntilCheckIn >= EARLY_BIRD_THRESHOLD_DAYS) {
            return EARLY_BIRD_DISCOUNT_PERCENTAGE;
        }

        return BigDecimal.ZERO;
    }

    /**
     * Apply last-minute and early-bird adjustments to the response
     */
    public DynamicPricingResponse applyTimingAdjustments(DynamicPricingResponse response, LocalDate checkIn) {
        BigDecimal lastMinuteAdjustment = calculateLastMinuteAdjustment(checkIn);
        BigDecimal earlyBirdDiscount = calculateEarlyBirdDiscount(checkIn);

        BigDecimal adjustedPrice = response.getFinalPrice();
        List<PricingAdjustment> allAdjustments = new ArrayList<>(response.getAdjustments());

        // Apply last-minute adjustment
        if (lastMinuteAdjustment.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amount = response.getBasePrice().multiply(lastMinuteAdjustment);
            allAdjustments.add(new PricingAdjustment(
                    "Last-Minute Booking",
                    lastMinuteAdjustment.multiply(new BigDecimal("100")),
                    amount,
                    "25% increase for bookings within 3 days"));
            adjustedPrice = adjustedPrice.add(amount);
        }

        // Apply early-bird discount
        if (earlyBirdDiscount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amount = response.getBasePrice().multiply(earlyBirdDiscount).negate();
            allAdjustments.add(new PricingAdjustment(
                    "Early-Bird Discount",
                    earlyBirdDiscount.multiply(new BigDecimal("100")).negate(),
                    amount,
                    "10% discount for bookings 30+ days in advance"));
            adjustedPrice = adjustedPrice.add(amount);
        }

        // Recalculate total adjustment amount
        BigDecimal totalAdjustmentAmount = adjustedPrice.subtract(response.getBasePrice());

        return DynamicPricingResponse.builder()
                .roomId(response.getRoomId())
                .basePrice(response.getBasePrice())
                .finalPrice(adjustedPrice.setScale(2, RoundingMode.HALF_UP))
                .adjustments(allAdjustments)
                .totalAdjustmentAmount(totalAdjustmentAmount.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
