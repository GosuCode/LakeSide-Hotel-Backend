package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.model.Room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author Simpson Alfred
 */

public interface IRoomService {
    Room addNewRoom(String photoUrl, String roomType, BigDecimal roomPrice);

    Room addNewRoom(String bedType, String roomType, int roomNumber, String description,
            String roomCategory, BigDecimal roomPrice, List<String> amenities,
            boolean isBooked, Long hotelId, String photoUrl);

    Room addNewRoomFromJson(Room room);

    List<String> getAllRoomTypes();

    List<Room> getAllRooms();

    void deleteRoom(Long roomId);

    Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, String photoUrl);

    Room updateRoomFull(Long roomId, Room roomUpdate);

    Optional<Room> getRoomById(Long roomId);

    List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType);
}
