package com.dailycodework.lakesidehotel.response;

import java.util.List;

import lombok.Data;

@Data
public class HotelResponse {
    private Long id;
    private String name;
    private String address;
    private String contact;
    private String email;
    private String website;
    private Integer roomsCount;
    private String description;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private List<RoomResponse> rooms;

    public HotelResponse() {
    }

    public HotelResponse(Long id, String name, String address, String contact,
            String email, String website, Integer roomsCount,
            String description, String imageUrl, Double latitude, Double longitude, List<RoomResponse> rooms) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.website = website;
        this.roomsCount = roomsCount;
        this.description = description;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rooms = rooms;
    }
}