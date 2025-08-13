package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.exception.InternalServerException;
import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Simpson Alfred
 */

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService {
    private final RoomRepository roomRepository;

    @Override
    public Room addNewRoom(String photoUrl, String roomType, BigDecimal roomPrice) {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            room.setPhotoUrl(photoUrl);
        }
        return roomRepository.save(room);
    }

    @Override
    public Room addNewRoom(String bedType, String roomType, int roomNumber, String description,
            String roomCategory, BigDecimal roomPrice, List<String> amenities,
            boolean isBooked, Long hotelId, String photoUrl) {
        Room room = new Room();
        room.setBedType(bedType);
        room.setRoomType(roomType);
        room.setRoomNumber(roomNumber);
        room.setDescription(description);
        room.setRoomCategory(roomCategory);
        room.setRoomPrice(roomPrice);
        room.setAmenities(amenities);
        room.setBooked(isBooked);

        // Set hotel if hotelId is provided
        if (hotelId != null) {
            // You'll need to inject HotelRepository to fetch the hotel
            // For now, we'll create a simple hotel object
            Hotel hotel = new Hotel();
            hotel.setId(hotelId);
            room.setHotel(hotel);
        }

        // Handle photo URL
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            room.setPhotoUrl(photoUrl);
        }

        return roomRepository.save(room);
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if (theRoom.isPresent()) {
            roomRepository.deleteById(roomId);
        }
    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, String photoUrl) {
        Room room = roomRepository.findById(roomId).get();
        if (roomType != null)
            room.setRoomType(roomType);
        if (roomPrice != null)
            room.setRoomPrice(roomPrice);
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            room.setPhotoUrl(photoUrl);
        }
        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return Optional.of(roomRepository.findById(roomId).get());
    }

    @Override
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        return roomRepository.findAvailableRoomsByDatesAndType(checkInDate, checkOutDate, roomType);
    }

    @Override
    public Room addNewRoomFromJson(Room room) {
        // Set default values if not provided
        if (room.getAmenities() == null) {
            room.setAmenities(new ArrayList<>());
        }
        if (room.getBookings() == null) {
            room.setBookings(new ArrayList<>());
        }

        return roomRepository.save(room);
    }

    @Override
    public Room updateRoomFull(Long roomId, Room roomUpdate) {
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));

        // Update all fields if they are provided
        if (roomUpdate.getRoomType() != null) {
            existingRoom.setRoomType(roomUpdate.getRoomType());
        }
        if (roomUpdate.getBedType() != null) {
            existingRoom.setBedType(roomUpdate.getBedType());
        }
        if (roomUpdate.getRoomNumber() != 0) {
            existingRoom.setRoomNumber(roomUpdate.getRoomNumber());
        }
        if (roomUpdate.getDescription() != null) {
            existingRoom.setDescription(roomUpdate.getDescription());
        }
        if (roomUpdate.getRoomCategory() != null) {
            existingRoom.setRoomCategory(roomUpdate.getRoomCategory());
        }
        if (roomUpdate.getRoomPrice() != null) {
            existingRoom.setRoomPrice(roomUpdate.getRoomPrice());
        }
        if (roomUpdate.getAmenities() != null) {
            existingRoom.setAmenities(roomUpdate.getAmenities());
        }
        if (roomUpdate.getPhotoUrl() != null && !roomUpdate.getPhotoUrl().trim().isEmpty()) {
            existingRoom.setPhotoUrl(roomUpdate.getPhotoUrl());
        }
        if (roomUpdate.getHotel() != null) {
            existingRoom.setHotel(roomUpdate.getHotel());
        }

        return roomRepository.save(existingRoom);
    }
}
