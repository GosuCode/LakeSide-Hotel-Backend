package com.dailycodework.lakesidehotel.config;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.repository.HotelRepository;
import com.dailycodework.lakesidehotel.service.KDTreeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DataInitializer but before DataSeeder
public class KDTreeInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final KDTreeService kdTreeService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing K-D Tree with existing hotel data...");

        try {
            // Sample hotel data with coordinates (you can replace with real coordinates)
            // For demo purposes, using some sample coordinates
            initializeSampleHotels();

            log.info("K-D Tree initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing K-D Tree: {}", e.getMessage(), e);
        }
    }

    private void initializeSampleHotels() {
        // Populate K-D Tree with all hotels (DataSeeder will create the hotels)
        Iterable<Hotel> hotels = hotelRepository.findAll();
        for (Hotel hotel : hotels) {
            if (hotel.getLatitude() != null && hotel.getLongitude() != null) {
                kdTreeService.updateHotelCache(hotel);
                log.debug("Added hotel to K-D Tree: {} at ({}, {})",
                        hotel.getName(), hotel.getLatitude(), hotel.getLongitude());
            }
        }
    }
}
