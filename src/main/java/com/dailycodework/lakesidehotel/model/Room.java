package com.dailycodework.lakesidehotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Simpson Alfred
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bedType;
    private String roomType;
    private int roomNumber;
    private String description;
    private String roomCategory;
    private BigDecimal roomPrice;
    private List<String> amenities;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    private String photoUrl;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookedRoom> bookings;

    public Room() {
        this.bookings = new ArrayList<>();
    }

    public void addBooking(BookedRoom booking) {
        if (bookings == null) {
            bookings = new ArrayList<>();
        }
        bookings.add(booking);
        booking.setRoom(this);

        String bookingCode = RandomStringUtils.randomNumeric(10);
        booking.setBookingConfirmationCode(bookingCode);
    }

    /**
     * Check if room is available for the given date range
     * 
     * @param checkInDate  requested check-in date
     * @param checkOutDate requested check-out date
     * @return true if room is available for the dates
     */
    public boolean isAvailableForDates(LocalDate checkInDate, LocalDate checkOutDate) {
        if (bookings == null || bookings.isEmpty()) {
            return true;
        }

        return bookings.stream()
                .noneMatch(booking -> (checkInDate.isBefore(booking.getCheckOutDate()) &&
                        checkOutDate.isAfter(booking.getCheckInDate())));
    }

    /**
     * Get current availability status (for display purposes)
     * 
     * @return true if room has any current or future bookings
     */
    public boolean hasCurrentBookings() {
        if (bookings == null || bookings.isEmpty()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return bookings.stream()
                .anyMatch(booking -> booking.getCheckOutDate().isAfter(today) ||
                        booking.getCheckOutDate().isEqual(today));
    }
}
