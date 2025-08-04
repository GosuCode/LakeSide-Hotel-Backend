package com.dailycodework.lakesidehotel.response;

import java.util.List;

import lombok.Data;

@Data
public class HotelResponse {
    private Long id;
    private String name;
    private String address;
    private String contact;
    private List<RoomResponse> rooms;

    public HotelResponse() {
    }

    public HotelResponse(Long id, String name, String address, String contact, List<RoomResponse> rooms) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.rooms = rooms;
    }
}