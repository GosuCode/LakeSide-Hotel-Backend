package com.dailycodework.lakesidehotel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private boolean isBooked;
    private String photo;
    private List<BookingResponse> bookings;

    private String bedType;
    private String description;
    private String roomCategory;
    private List<String> amenities;
    private Long hotelId;
    private HotelResponse hotel;

    public RoomResponse(Long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }

    public RoomResponse(Long id, int roomNumber, String roomType, BigDecimal roomPrice, boolean isBooked,
            String photoUrl, List<BookingResponse> bookings) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photo = photoUrl;
        this.bookings = bookings;
    }

    // New constructor with all fields
    public RoomResponse(Long id, int roomNumber, String roomType, BigDecimal roomPrice, boolean isBooked,
            String photoUrl, List<BookingResponse> bookings, String bedType, String description,
            String roomCategory, List<String> amenities, Long hotelId) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photo = photoUrl;
        this.bookings = bookings;
        this.bedType = bedType;
        this.description = description;
        this.roomCategory = roomCategory;
        this.amenities = amenities;
        this.hotelId = hotelId;
    }

    // Constructor with hotel object
    public RoomResponse(Long id, int roomNumber, String roomType, BigDecimal roomPrice, boolean isBooked,
            String photoUrl, List<BookingResponse> bookings, String bedType, String description,
            String roomCategory, List<String> amenities, Long hotelId, HotelResponse hotel) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photo = photoUrl;
        this.bookings = bookings;
        this.bedType = bedType;
        this.description = description;
        this.roomCategory = roomCategory;
        this.amenities = amenities;
        this.hotelId = hotelId;
        this.hotel = hotel;
    }
}
