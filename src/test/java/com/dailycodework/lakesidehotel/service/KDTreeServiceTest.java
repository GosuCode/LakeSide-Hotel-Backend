package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.response.NearbyHotelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KDTreeServiceTest {

    private KDTreeService kdTreeService;

    @BeforeEach
    void setUp() {
        kdTreeService = new KDTreeService();
    }

    @Test
    void testInsertAndFindHotels() {
        // Insert test hotels
        kdTreeService.insertHotel(1L, 40.7128, -74.0060); // New York
        kdTreeService.insertHotel(2L, 40.7589, -73.9851); // Times Square
        kdTreeService.insertHotel(3L, 40.7505, -73.9934); // Penn Station

        // Create hotel objects for cache
        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Hotel 1");
        hotel1.setLatitude(40.7128);
        hotel1.setLongitude(-74.0060);

        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Hotel 2");
        hotel2.setLatitude(40.7589);
        hotel2.setLongitude(-73.9851);

        Hotel hotel3 = new Hotel();
        hotel3.setId(3L);
        hotel3.setName("Hotel 3");
        hotel3.setLatitude(40.7505);
        hotel3.setLongitude(-73.9934);

        // Update cache
        kdTreeService.updateHotelCache(hotel1);
        kdTreeService.updateHotelCache(hotel2);
        kdTreeService.updateHotelCache(hotel3);

        // Test finding nearest hotels from a point near Hotel 1
        List<NearbyHotelResponse> nearest = kdTreeService.findNearestHotels(40.7130, -74.0062, 2);

        assertNotNull(nearest);
        assertEquals(2, nearest.size());

        // Hotel 1 should be the nearest
        assertEquals(1L, nearest.get(0).getId());
        assertTrue(nearest.get(0).getDistance() < nearest.get(1).getDistance());
    }

    @Test
    void testDistanceCalculation() {
        // Test distance between two points (approximately 5.5 km apart)
        kdTreeService.insertHotel(1L, 40.7128, -74.0060); // New York
        kdTreeService.insertHotel(2L, 40.7589, -73.9851); // Times Square

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Hotel 1");
        hotel1.setLatitude(40.7128);
        hotel1.setLongitude(-74.0060);

        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Hotel 2");
        hotel2.setLatitude(40.7589);
        hotel2.setLongitude(-73.9851);

        kdTreeService.updateHotelCache(hotel1);
        kdTreeService.updateHotelCache(hotel2);

        List<NearbyHotelResponse> nearest = kdTreeService.findNearestHotels(40.7128, -74.0060, 2);

        assertEquals(2, nearest.size());
        assertEquals(0.0, nearest.get(0).getDistance(), 0.1); // Should be at same location
        assertTrue(nearest.get(1).getDistance() > 0); // Should have some distance
    }
}
