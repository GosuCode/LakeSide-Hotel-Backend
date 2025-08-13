package com.dailycodework.lakesidehotel.controller;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.response.HotelResponse;
import com.dailycodework.lakesidehotel.response.NearbyHotelResponse;
import com.dailycodework.lakesidehotel.service.IHotelService;
import com.dailycodework.lakesidehotel.service.KDTreeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final IHotelService hotelService;
    private final KDTreeService kdTreeService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HotelResponse> addHotel(@RequestBody Hotel hotel) {
        try {
            HotelResponse savedHotel = hotelService.createHotel(hotel);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedHotel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        try {
            List<HotelResponse> hotels = hotelService.getAllHotels();
            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        try {
            HotelResponse hotel = hotelService.getHotelById(id);
            return ResponseEntity.ok(hotel);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        try {
            HotelResponse updatedHotel = hotelService.updateHotel(id, hotel);
            return ResponseEntity.ok(updatedHotel);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        try {
            hotelService.deleteHotel(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address) {
        try {
            List<HotelResponse> hotels = hotelService.getAllHotels();

            // Filter by name if provided
            if (name != null && !name.trim().isEmpty()) {
                hotels = hotels.stream()
                        .filter(hotel -> hotel.getName() != null &&
                                hotel.getName().toLowerCase().contains(name.toLowerCase()))
                        .toList();
            }

            // Filter by address if provided
            if (address != null && !address.trim().isEmpty()) {
                hotels = hotels.stream()
                        .filter(hotel -> hotel.getAddress() != null &&
                                hotel.getAddress().toLowerCase().contains(address.toLowerCase()))
                        .toList();
            }

            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyHotelResponse>> findNearbyHotels(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") int k) {
        try {
            if (k <= 0 || k > 100) {
                return ResponseEntity.badRequest().build();
            }

            List<NearbyHotelResponse> nearbyHotels = kdTreeService.findNearestHotels(lat, lon, k);
            return ResponseEntity.ok(nearbyHotels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Hotel service is running!");
    }
}
