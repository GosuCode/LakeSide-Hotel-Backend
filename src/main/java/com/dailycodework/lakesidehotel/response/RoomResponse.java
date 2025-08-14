package com.dailycodework.lakesidehotel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.dailycodework.lakesidehotel.response.HotelResponse;

/**
 * @author Simpson Alfred
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Long id;
    private String roomType;
    private BigDecimal roomPrice;
    private int roomNumber;
    private String photo;
    private List<BookingResponse> bookings;

    private String bedType;
    private String description;
    private String roomCategory;
    private List<String> amenities;
    private Long hotelId;
    private HotelResponse hotel;

    // Computed availability fields
    private boolean isAvailableForDates;
    private boolean hasCurrentBookings;

    public RoomResponse(Long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }

    public RoomResponse(Long id, int roomNumber, String roomType, BigDecimal roomPrice, String photoUrl,
            List<BookingResponse> bookings) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.photo = photoUrl;
        this.bookings = bookings;
    }

    // New constructor with all fields
    public RoomResponse(Long id, String bedType, String roomType, int roomNumber, String description,
            String roomCategory, BigDecimal roomPrice, List<String> amenities, Long hotelId,
            HotelResponse hotel, String photoUrl, List<BookingResponse> bookings) {
        this.id = id;
        this.bedType = bedType;
        this.roomType = roomType;
        this.roomNumber = roomNumber;
        this.description = description;
        this.roomCategory = roomCategory;
        this.roomPrice = roomPrice;
        this.amenities = amenities;
        this.hotelId = hotelId;
        this.hotel = hotel;
        this.photo = photoUrl;
        this.bookings = bookings;
    }

    /**
     * Set availability for specific dates
     */
    public void setAvailabilityForDates(LocalDate checkIn, LocalDate checkOut) {
        if (bookings == null || bookings.isEmpty()) {
            this.isAvailableForDates = true;
            return;
        }

        this.isAvailableForDates = bookings.stream()
                .noneMatch(booking -> {
                    LocalDate bookingCheckIn = booking.getCheckInDate();
                    LocalDate bookingCheckOut = booking.getCheckOutDate();
                    return (checkIn.isBefore(bookingCheckOut) && checkOut.isAfter(bookingCheckIn));
                });
    }

    /**
     * Set current availability status
     */
    public void setCurrentAvailability() {
        if (bookings == null || bookings.isEmpty()) {
            this.hasCurrentBookings = false;
            return;
        }

        LocalDate today = LocalDate.now();
        this.hasCurrentBookings = bookings.stream()
                .anyMatch(booking -> {
                    LocalDate checkOut = booking.getCheckOutDate();
                    return checkOut.isAfter(today) || checkOut.isEqual(today);
                });
    }
}
