package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.response.DynamicPricingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DynamicPricingService
 * Note: This is a basic test structure. In a real project, you'd use @MockBean
 * and proper unit testing.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DynamicPricingServiceTest {

    @Test
    public void testSeasonalDateCalculation() {
        // Test December 15-31 (should be seasonal)
        assertTrue(isSeasonalDate(LocalDate.of(2024, 12, 15)));
        assertTrue(isSeasonalDate(LocalDate.of(2024, 12, 25)));
        assertTrue(isSeasonalDate(LocalDate.of(2024, 12, 31)));

        // Test January 1-5 (should be seasonal)
        assertTrue(isSeasonalDate(LocalDate.of(2025, 1, 1)));
        assertTrue(isSeasonalDate(LocalDate.of(2025, 1, 5)));

        // Test non-seasonal dates (should not be seasonal)
        assertFalse(isSeasonalDate(LocalDate.of(2024, 12, 14)));
        assertFalse(isSeasonalDate(LocalDate.of(2025, 1, 6)));
        assertFalse(isSeasonalDate(LocalDate.of(2024, 11, 15)));
        assertFalse(isSeasonalDate(LocalDate.of(2025, 2, 1)));
    }

    @Test
    public void testWeekendCalculation() {
        // Test weekend dates (Friday and Saturday)
        assertTrue(isWeekend(LocalDate.of(2024, 12, 20))); // Friday
        assertTrue(isWeekend(LocalDate.of(2024, 12, 21))); // Saturday

        // Test non-weekend dates
        assertFalse(isWeekend(LocalDate.of(2024, 12, 22))); // Sunday
        assertFalse(isWeekend(LocalDate.of(2024, 12, 23))); // Monday
    }

    @Test
    public void testLastMinuteCalculation() {
        LocalDate today = LocalDate.now();

        // Test last-minute (within 3 days)
        assertTrue(isLastMinute(today.plusDays(1)));
        assertTrue(isLastMinute(today.plusDays(3)));

        // Test not last-minute
        assertFalse(isLastMinute(today.plusDays(4)));
        assertFalse(isLastMinute(today.plusDays(10)));
    }

    @Test
    public void testEarlyBirdCalculation() {
        LocalDate today = LocalDate.now();

        // Test early-bird (30+ days in advance)
        assertTrue(isEarlyBird(today.plusDays(30)));
        assertTrue(isEarlyBird(today.plusDays(60)));

        // Test not early-bird
        assertFalse(isEarlyBird(today.plusDays(29)));
        assertFalse(isEarlyBird(today.plusDays(15)));
    }

    // Helper methods for testing (copied from service for testing purposes)
    private boolean isSeasonalDate(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if (month == 12 && day >= 15) {
            return true;
        }

        if (month == 1 && day <= 5) {
            return true;
        }

        return false;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 5; // Friday = 5, Saturday = 6
    }

    private boolean isLastMinute(LocalDate checkIn) {
        long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
        return daysUntilCheckIn <= 3 && daysUntilCheckIn >= 0;
    }

    private boolean isEarlyBird(LocalDate checkIn) {
        long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
        return daysUntilCheckIn >= 30;
    }
}
