package com.dailycodework.lakesidehotel.config;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.repository.HotelRepository;
import com.dailycodework.lakesidehotel.service.KDTreeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
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
        // Add some sample hotels with coordinates if none exist
        if (hotelRepository.count() == 0) {
            log.info("No hotels found, adding sample data...");

            // Sample hotels with coordinates (replace with real data)
            Hotel hotel1 = new Hotel();
            hotel1.setName("LakeSide Hotel");
            hotel1.setAddress("123 Lake Street");
            hotel1.setLatitude(40.7128);
            hotel1.setLongitude(-74.0060);
            hotel1.setContact("+1-555-0123");
            hotel1.setEmail("info@lakeside.com");
            hotel1.setWebsite("www.lakeside.com");
            hotel1.setRoomsCount(50);
            hotel1.setDescription("Beautiful lakeside hotel with stunning views");
            hotelRepository.save(hotel1);

            Hotel hotel2 = new Hotel();
            hotel2.setName("Mountain View Inn");
            hotel2.setAddress("456 Mountain Road");
            hotel2.setLatitude(40.7589);
            hotel2.setLongitude(-73.9851);
            hotel2.setContact("+1-555-0456");
            hotel2.setEmail("info@mountainview.com");
            hotel2.setWebsite("www.mountainview.com");
            hotel2.setRoomsCount(30);
            hotel2.setDescription("Cozy inn with mountain views");
            hotelRepository.save(hotel2);

            Hotel hotel3 = new Hotel();
            hotel3.setName("City Center Hotel");
            hotel3.setAddress("789 Downtown Ave");
            hotel3.setLatitude(40.7505);
            hotel3.setLongitude(-73.9934);
            hotel3.setContact("+1-555-0789");
            hotel3.setEmail("info@citycenter.com");
            hotel3.setWebsite("www.citycenter.com");
            hotel3.setRoomsCount(100);
            hotel3.setDescription("Modern hotel in the heart of the city");
            hotelRepository.save(hotel3);
        }

        // Populate K-D Tree with all hotels
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
