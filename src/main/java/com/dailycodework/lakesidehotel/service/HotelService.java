package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.repository.HotelRepository;
import com.dailycodework.lakesidehotel.response.BookingResponse;
import com.dailycodework.lakesidehotel.response.HotelResponse;
import com.dailycodework.lakesidehotel.response.RoomResponse;
import com.dailycodework.lakesidehotel.service.KDTreeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService implements IHotelService {

    private final HotelRepository hotelRepository;
    private final KDTreeService kdTreeService;

    @Override
    public HotelResponse createHotel(Hotel hotel) {
        Hotel saved = hotelRepository.save(hotel);
        // Update K-D Tree if coordinates are available
        if (saved.getLatitude() != null && saved.getLongitude() != null) {
            kdTreeService.updateHotelCache(saved);
        }
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

        // Update all fields
        existing.setName(updatedHotel.getName());
        existing.setAddress(updatedHotel.getAddress());
        existing.setContact(updatedHotel.getContact());
        existing.setEmail(updatedHotel.getEmail());
        existing.setWebsite(updatedHotel.getWebsite());
        existing.setRoomsCount(updatedHotel.getRoomsCount());
        existing.setDescription(updatedHotel.getDescription());
        existing.setImageUrl(updatedHotel.getImageUrl());
        existing.setLatitude(updatedHotel.getLatitude());
        existing.setLongitude(updatedHotel.getLongitude());

        Hotel saved = hotelRepository.save(existing);

        // Update K-D Tree if coordinates are available
        if (saved.getLatitude() != null && saved.getLongitude() != null) {
            kdTreeService.updateHotelCache(saved);
        }

        return mapToResponse(saved);
    }

    @Override
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new EntityNotFoundException("Hotel not found with id " + id);
        }
        kdTreeService.removeHotel(id);
        hotelRepository.deleteById(id);
    }

    private HotelResponse mapToResponse(Hotel hotel) {
        List<RoomResponse> roomResponses = hotel.getRooms() == null ? List.of()
                : hotel.getRooms().stream().map(room -> {
                    return new RoomResponse(
                            room.getId(),
                            room.getRoomNumber(),
                            room.getRoomType(),
                            room.getRoomPrice(),
                            room.isBooked(),
                            room.getPhotoUrl(),
                            room.getBookings().stream()
                                    .map(booking -> new BookingResponse(booking.getBookingId(),
                                            booking.getCheckInDate(),
                                            booking.getCheckOutDate(), booking.getBookingConfirmationCode()))
                                    .collect(Collectors.toList()));
                }).collect(Collectors.toList());

        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getContact(),
                hotel.getEmail(),
                hotel.getWebsite(),
                hotel.getRoomsCount(),
                hotel.getDescription(),
                hotel.getImageUrl(),
                roomResponses);
    }
}