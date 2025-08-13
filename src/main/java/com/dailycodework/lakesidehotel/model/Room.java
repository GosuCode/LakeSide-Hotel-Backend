package com.dailycodework.lakesidehotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
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
    private boolean isBooked = false;

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
        isBooked = true;

        String bookingCode = RandomStringUtils.randomNumeric(10);
        booking.setBookingConfirmationCode(bookingCode);
    }

}
