package com.dailycodework.lakesidehotel.controller;

import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.BookedRoom;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.response.BookingResponse;
import com.dailycodework.lakesidehotel.response.DynamicPricingResponse;
import com.dailycodework.lakesidehotel.response.RoomResponse;
import com.dailycodework.lakesidehotel.service.BookingService;
import com.dailycodework.lakesidehotel.service.HotelService;
import com.dailycodework.lakesidehotel.service.IDynamicPricingService;
import com.dailycodework.lakesidehotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Simpson Alfred
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
    private final IRoomService roomService;
    private final BookingService bookingService;
    private final IDynamicPricingService dynamicPricingService;
    private final HotelService hotelService;

    @PostMapping("/add/new-room")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("bedType") String bedType,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomNumber") int roomNumber,
            @RequestParam("description") String description,
            @RequestParam("roomCategory") String roomCategory,
            @RequestParam("roomPrice") BigDecimal roomPrice,
            @RequestParam(value = "amenities", required = false) String amenitiesString,
            @RequestParam(value = "hotel.id", required = false) Long hotelId,
            @RequestParam(value = "photoUrl", required = false) String photoUrl) {

        // Parse amenities from comma-separated string
        List<String> amenities = null;
        if (amenitiesString != null && !amenitiesString.trim().isEmpty()) {
            amenities = List.of(amenitiesString.split(","));
        }

        Room savedRoom = roomService.addNewRoom(
                bedType, roomType, roomNumber, description, roomCategory,
                roomPrice, amenities, hotelId, photoUrl);

        // Convert Hotel to HotelResponse if hotel exists
        com.dailycodework.lakesidehotel.response.HotelResponse hotelResponse = null;
        if (savedRoom.getHotel() != null) {
            hotelResponse = hotelService.getHotelById(savedRoom.getHotel().getId());
        }

        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getBedType(),
                savedRoom.getRoomType(), savedRoom.getRoomNumber(), savedRoom.getDescription(),
                savedRoom.getRoomCategory(), savedRoom.getRoomPrice(), savedRoom.getAmenities(),
                savedRoom.getHotel() != null ? savedRoom.getHotel().getId() : null,
                hotelResponse, savedRoom.getPhotoUrl(), null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add/new-room-json")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> addNewRoomJson(@RequestBody Room room) {
        Room savedRoom = roomService.addNewRoomFromJson(room);

        // Convert Hotel to HotelResponse if hotel exists
        com.dailycodework.lakesidehotel.response.HotelResponse hotelResponse = null;
        if (savedRoom.getHotel() != null) {
            hotelResponse = hotelService.getHotelById(savedRoom.getHotel().getId());
        }

        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getBedType(),
                savedRoom.getRoomType(), savedRoom.getRoomNumber(), savedRoom.getDescription(),
                savedRoom.getRoomCategory(), savedRoom.getRoomPrice(), savedRoom.getAmenities(),
                savedRoom.getHotel() != null ? savedRoom.getHotel().getId() : null,
                hotelResponse, savedRoom.getPhotoUrl(), null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-auth")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("Authentication successful! You have ROLE_ADMIN access.");
    }

    @GetMapping("/room/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : rooms) {
            RoomResponse roomResponse = getRoomResponse(room);
            roomResponses.add(roomResponse);
        }
        return ResponseEntity.ok(roomResponses);
    }

    @DeleteMapping("/delete/room/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) BigDecimal roomPrice,
            @RequestParam(required = false) String photoUrl) {
        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoUrl);
        RoomResponse roomResponse = getRoomResponse(theRoom);
        return ResponseEntity.ok(roomResponse);
    }

    @PutMapping("/update-full/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> updateRoomFull(@PathVariable Long roomId,
            @RequestBody Room roomUpdate) {
        Room theRoom = roomService.updateRoomFull(roomId, roomUpdate);
        RoomResponse roomResponse = getRoomResponse(theRoom);
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam("roomType") String roomType) {
        List<Room> availableRooms = roomService.getAvailableRooms(checkInDate, checkOutDate, roomType);
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : availableRooms) {
            RoomResponse roomResponse = getRoomResponse(room);
            roomResponses.add(roomResponse);
        }
        if (roomResponses.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(roomResponses);
        }
    }

    @GetMapping("/pricing/{roomId}")
    public ResponseEntity<DynamicPricingResponse> getRoomPricing(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        try {
            DynamicPricingResponse pricing = dynamicPricingService.calculatePrice(roomId, checkIn, checkOut);
            // Apply timing-based adjustments (last-minute, early-bird)
            pricing = dynamicPricingService.applyTimingAdjustments(pricing, checkIn);
            return ResponseEntity.ok(pricing);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search-with-pricing")
    public ResponseEntity<List<DynamicPricingResponse>> searchRoomsWithPricing(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) String roomType) {

        try {
            List<Room> availableRooms;
            if (roomType != null && !roomType.trim().isEmpty()) {
                availableRooms = roomService.getAvailableRooms(checkIn, checkOut, roomType);
            } else {
                availableRooms = roomService.getAvailableRooms(checkIn, checkOut, null);
            }

            List<DynamicPricingResponse> pricingResponses = new ArrayList<>();
            for (Room room : availableRooms) {
                DynamicPricingResponse pricing = dynamicPricingService.calculatePrice(room.getId(), checkIn, checkOut);
                pricing = dynamicPricingService.applyTimingAdjustments(pricing, checkIn);
                pricingResponses.add(pricing);
            }

            return ResponseEntity.ok(pricingResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(booking -> new BookingResponse(booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(), booking.getBookingConfirmationCode()))
                .toList();

        // Convert Hotel to HotelResponse if hotel exists
        com.dailycodework.lakesidehotel.response.HotelResponse hotelResponse = null;
        if (room.getHotel() != null) {
            hotelResponse = hotelService.getHotelById(room.getHotel().getId());
        }

        RoomResponse roomResponse = new RoomResponse(room.getId(), room.getBedType(),
                room.getRoomType(), room.getRoomNumber(), room.getDescription(),
                room.getRoomCategory(), room.getRoomPrice(), room.getAmenities(),
                room.getHotel() != null ? room.getHotel().getId() : null,
                hotelResponse, room.getPhotoUrl(), bookingInfo);

        // Set availability status
        roomResponse.setCurrentAvailability();

        return roomResponse;
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }
}
