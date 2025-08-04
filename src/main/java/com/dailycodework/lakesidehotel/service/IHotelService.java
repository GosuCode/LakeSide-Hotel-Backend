package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.response.HotelResponse;

import java.util.List;

public interface IHotelService {
    HotelResponse createHotel(Hotel hotel);

    HotelResponse getHotelById(Long id);

    List<HotelResponse> getAllHotels();

    HotelResponse updateHotel(Long id, Hotel updatedHotel);

    void deleteHotel(Long id);
}
