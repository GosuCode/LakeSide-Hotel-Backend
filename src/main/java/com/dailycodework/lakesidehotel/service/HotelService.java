package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.repository.HotelRepository;
import com.dailycodework.lakesidehotel.response.BookingResponse;
import com.dailycodework.lakesidehotel.response.HotelResponse;
import com.dailycodework.lakesidehotel.response.RoomResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelService implements IHotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Override
    public HotelResponse createHotel(Hotel hotel) {
        Hotel saved = hotelRepository.save(hotel);
        return mapToResponse(saved);
    }

    @Override
    public HotelResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id " + id));
        return mapToResponse(hotel);
    }

    @Override
    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HotelResponse updateHotel(Long id, Hotel updatedHotel) {
        Hotel existing = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id " + id));

        existing.setName(updatedHotel.getName());
        existing.setAddress(updatedHotel.getAddress());
        existing.setContact(updatedHotel.getContact());

        return mapToResponse(hotelRepository.save(existing));
    }

    @Override
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new EntityNotFoundException("Hotel not found with id " + id);
        }
        hotelRepository.deleteById(id);
    }

    private HotelResponse mapToResponse(Hotel hotel) {
        List<RoomResponse> roomResponses = hotel.getRooms() == null ? List.of()
                : hotel.getRooms().stream().map(room -> {
                    try {
                        byte[] photoBytes = room.getPhoto() != null
                                ? room.getPhoto().getBytes(1, (int) room.getPhoto().length())
                                : null;
                        return new RoomResponse(
                                room.getId(),
                                room.getRoomNumber(),
                                room.getRoomType(),
                                room.getRoomPrice(),
                                room.isBooked(),
                                photoBytes,
                                room.getBookings().stream()
                                        .map(booking -> new BookingResponse(booking.getBookingId(),
                                                booking.getCheckInDate(),
                                                booking.getCheckOutDate(), booking.getBookingConfirmationCode()))
                                        .collect(Collectors.toList()));
                    } catch (Exception e) {
                        return new RoomResponse(
                                room.getId(),
                                room.getRoomNumber(),
                                room.getRoomType(),
                                room.getRoomPrice(),
                                room.isBooked(),
                                null,
                                room.getBookings().stream()
                                        .map(booking -> new BookingResponse(booking.getBookingId(),
                                                booking.getCheckInDate(),
                                                booking.getCheckOutDate(), booking.getBookingConfirmationCode()))
                                        .collect(Collectors.toList()));
                    }
                }).collect(Collectors.toList());

        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getContact(),
                roomResponses);
    }
}