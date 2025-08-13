package com.dailycodework.lakesidehotel.model;

import com.dailycodework.lakesidehotel.config.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookedRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private String guestFullName;
    private String guestEmail;

    private int numOfAdults;
    private int numOfChildren;
    private int totalNumOfGuests;

    // New pricing fields
    private BigDecimal basePricePerNight;
    private BigDecimal finalPricePerNight;
    private BigDecimal totalAmount;
    private int numberOfNights;

    // Store pricing adjustments as JSON string
    @Column(columnDefinition = "TEXT")
    private String pricingAdjustments;

    @Column(unique = true)
    private String bookingConfirmationCode;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    public void calculateTotalGuests() {
        this.totalNumOfGuests = this.numOfAdults + this.numOfChildren;
    }

    public void setNumOfAdults(int numOfAdults) {
        this.numOfAdults = numOfAdults;
        calculateTotalGuests();
    }

    public void setNumOfChildren(int numOfChildren) {
        this.numOfChildren = numOfChildren;
        calculateTotalGuests();
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    // New method to calculate total amount
    public void calculateTotalAmount() {
        if (this.finalPricePerNight != null && this.numberOfNights > 0) {
            this.totalAmount = this.finalPricePerNight.multiply(BigDecimal.valueOf(this.numberOfNights));
        }
    }

    // Method to set dates and calculate nights
    public void setCheckInAndCheckOut(LocalDate checkIn, LocalDate checkOut) {
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.numberOfNights = checkOut.getDayOfYear() - checkIn.getDayOfYear();
        calculateTotalAmount();
    }
}
