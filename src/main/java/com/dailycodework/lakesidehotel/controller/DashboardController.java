package com.dailycodework.lakesidehotel.controller;

import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.service.BookingService;
import com.dailycodework.lakesidehotel.service.HotelService;
import com.dailycodework.lakesidehotel.service.IRoomService;
import com.dailycodework.lakesidehotel.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class DashboardController {

    private final BookingService bookingService;
    private final HotelService hotelService;
    private final IRoomService roomService;
    private final IUserService userService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Total counts
            stats.put("totalBookings", bookingService.getAllBookings().size());
            stats.put("totalHotels", hotelService.getAllHotels().size());
            stats.put("totalRooms", roomService.getAllRooms().size());
            stats.put("totalUsers", userService.getUsers().size());

            // Revenue calculation (sum of all booking amounts)
            BigDecimal totalRevenue = bookingService.getAllBookings().stream()
                    .map(booking -> booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);

            // Recent bookings count (last 30 days)
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            long recentBookings = bookingService.getAllBookings().stream()
                    .filter(booking -> booking.getCheckInDate().isAfter(thirtyDaysAgo))
                    .count();
            stats.put("recentBookings", recentBookings);

            // Available rooms count
            long availableRooms = roomService.getAllRooms().stream()
                    .filter(room -> !room.hasCurrentBookings())
                    .count();
            stats.put("availableRooms", availableRooms);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookings-by-month")
    public ResponseEntity<List<Map<String, Object>>> getBookingsByMonth() {
        try {
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            LocalDate now = LocalDate.now();

            for (int i = 11; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                String monthName = month.format(DateTimeFormatter.ofPattern("MMM yyyy"));

                long bookingsCount = bookingService.getAllBookings().stream()
                        .filter(booking -> {
                            LocalDate bookingDate = booking.getCheckInDate();
                            return bookingDate.getMonth() == month.getMonth() &&
                                    bookingDate.getYear() == month.getYear();
                        })
                        .count();

                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", monthName);
                monthData.put("bookings", bookingsCount);
                monthlyData.add(monthData);
            }

            return ResponseEntity.ok(monthlyData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/room-occupancy")
    public ResponseEntity<List<Map<String, Object>>> getRoomOccupancy() {
        try {
            List<Map<String, Object>> occupancyData = new ArrayList<>();

            // Get room types and their occupancy
            Map<String, Long> roomTypeCounts = roomService.getAllRooms().stream()
                    .collect(Collectors.groupingBy(Room::getRoomType, Collectors.counting()));

            Map<String, Long> occupiedRoomCounts = roomService.getAllRooms().stream()
                    .filter(Room::hasCurrentBookings)
                    .collect(Collectors.groupingBy(Room::getRoomType, Collectors.counting()));

            for (String roomType : roomTypeCounts.keySet()) {
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("roomType", roomType);
                typeData.put("total", roomTypeCounts.get(roomType));
                typeData.put("occupied", occupiedRoomCounts.getOrDefault(roomType, 0L));
                typeData.put("available", roomTypeCounts.get(roomType) - occupiedRoomCounts.getOrDefault(roomType, 0L));

                occupancyData.add(typeData);
            }

            return ResponseEntity.ok(occupancyData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/recent-bookings")
    public ResponseEntity<List<Map<String, Object>>> getRecentBookings() {
        try {
            List<Map<String, Object>> recentBookings = bookingService.getAllBookings().stream()
                    .sorted((a, b) -> b.getCheckInDate().compareTo(a.getCheckInDate()))
                    .limit(10)
                    .map(booking -> {
                        Map<String, Object> bookingData = new HashMap<>();
                        bookingData.put("id", booking.getBookingId());
                        bookingData.put("guestName", booking.getGuestFullName());
                        bookingData.put("guestEmail", booking.getGuestEmail());
                        bookingData.put("checkIn", booking.getCheckInDate().toString());
                        bookingData.put("checkOut", booking.getCheckOutDate().toString());
                        bookingData.put("roomType", booking.getRoom().getRoomType());
                        bookingData.put("totalAmount", booking.getTotalAmount());
                        bookingData.put("confirmationCode", booking.getBookingConfirmationCode());
                        return bookingData;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(recentBookings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/top-performing-hotels")
    public ResponseEntity<List<Map<String, Object>>> getTopPerformingHotels() {
        try {
            Map<Long, Long> hotelBookingCounts = bookingService.getAllBookings().stream()
                    .collect(Collectors.groupingBy(
                            booking -> booking.getRoom().getHotel().getId(),
                            Collectors.counting()));

            List<Map<String, Object>> topHotels = hotelBookingCounts.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> hotelData = new HashMap<>();
                        hotelData.put("hotelId", entry.getKey());
                        hotelData.put("bookingCount", entry.getValue());
                        return hotelData;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(topHotels);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
}
