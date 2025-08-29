package com.dailycodework.lakesidehotel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private Long id;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private String guestName;

    private String guestEmail;

    private String guestPhone;

    private int numOfAdults;

    private int numOfChildren;

    private int totalNumOfGuests;

    private int numOfNights;

    private String bookingConfirmationCode;

    private RoomResponse room;

    public BookingResponse(Long id, LocalDate checkInDate, LocalDate checkOutDate, String confirmationCode) {
        this.id = id;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingConfirmationCode = confirmationCode;
        this.guestName = "";
        this.guestEmail = "";
        this.guestPhone = "";
        this.numOfAdults = 0;
        this.numOfChildren = 0;
        this.totalNumOfGuests = 0;
        this.numOfNights = 0;
        this.room = null;
    }
}
