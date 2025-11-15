package com.dailycodework.lakesidehotel.config;

import com.dailycodework.lakesidehotel.model.*;
import com.dailycodework.lakesidehotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run first - handles all data initialization
public class DataSeeder implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only run seeding when explicitly requested via system property
        String seedData = System.getProperty("seed.data");
        if (!"true".equals(seedData)) {
            log.info("DataSeeder skipped - seed.data property not set");
            return;
        }

        log.info("Starting comprehensive data seeding...");

        try {
            // Always clear existing data first
            log.warn("Clearing all existing data...");
            bookingRepository.deleteAll();
            roomRepository.deleteAll();
            hotelRepository.deleteAll();
            userRepository.deleteAll();
            roleRepository.deleteAll();
            log.info("Existing data cleared successfully");

            // Seed roles first
            seedRoles();

            // Seed hotels
            seedHotels();

            // Seed rooms for all hotels
            seedRooms();

            // Seed users
            seedUsers();

            // Seed bookings
            seedBookings();

            log.info("Data seeding completed successfully");
        } catch (Exception e) {
            log.error("Error during data seeding: {}", e.getMessage(), e);
        }
    }

    private void seedRoles() {
        log.info("Seeding roles...");

        createRoleIfNotExists("ROLE_USER");
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_MANAGER");
        createRoleIfNotExists("ROLE_RECEPTIONIST");
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.findByName(roleName).isPresent()) {
            Role role = new Role(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }

    private void seedHotels() {
        log.info("Seeding Butwal, Nepal hotels...");

        List<Hotel> hotels = Arrays.asList(
                createHotel("Club De Novo Hotel", "Kalika Nagar, Butwal, Nepal",
                        27.7000, 83.4500, "+977-71-438885", "info@clubdenovo.com",
                        "www.clubdenovo.com", 45,
                        "Modern hotel with outdoor swimming pool, fitness center, free Wi-Fi, and air-conditioned rooms with cable TV. Features meeting facilities, shared lounge, and free parking."),

                createHotel("Hotel Avenue", "Golpark, Butwal, Nepal",
                        27.7010, 83.4510, "+977-71-533822", "info@hotelavenue.com",
                        "www.hotelavenue.com", 160,
                        "Large hotel with 160 deluxe guest rooms featuring modern amenities, television, internet access, international direct-dial phones, and safes. Includes elevator, free Wi-Fi, and on-site restaurant."),

                createHotel("Darcy's International Hotel", "New Buspark, Butwal, Nepal",
                        27.6990, 83.4490, "+977-71-553800", "info@darcyshotel.com",
                        "www.darcyshotel.com", 60,
                        "3-star hotel with restaurant, free private parking, bar, 24-hour front desk, room service, tour organization, ATM, concierge service, and air-conditioned rooms with flat-screen TVs."),

                createHotel("Hotel Da Flamingo", "Yogikuti, Butwal, Nepal",
                        27.7020, 83.4520, "+977-71-540000", "info@hoteldaflamingo.com",
                        "www.hoteldaflamingo.com", 80,
                        "Luxury hotel offering executive suites, junior suites, deluxe and superior rooms. Features gym, swimming pool, rooftop and poolside bars, café, restaurant, and event spaces for social and business events."),

                createHotel("Hotel Royal Chautari", "Buddha Path, Butwal, Nepal",
                        27.6980, 83.4480, "+977-71-520000", "info@royalchautari.com",
                        "www.royalchautari.com", 40,
                        "Charming hotel with restaurant, free private parking, bar, non-smoking rooms, shuttle service, free Wi-Fi, air conditioning, snack bar, daily maid service, and BBQ facilities."),

                createHotel("Tiger Palace Resort", "Tilottama, Siddhartha Nagar, Nepal",
                        27.7200, 83.4700, "+977-71-580000", "info@tigerpalace.com",
                        "www.tigerpalace.com", 100,
                        "Luxury resort with 100 luxurious rooms and 2 opulent villas. Features casino, spa, fitness center, multiple meeting rooms, six restaurants and bars, swimming pool, and facilities for destination weddings."),

                createHotel("Lumbini Palace Resort", "Siyari, Rupandehi, Nepal",
                        27.6800, 83.4200, "+977-71-590000", "info@lumbinipalace.com",
                        "www.lumbinipalace.com", 50,
                        "Unique resort offering deluxe and pond rooms situated on an island in the middle of a pond. Features organic farm, meditation hall, seminar rooms, fishing, cycling, and boating activities."),

                createHotel("Bodhi Redsun-Shinee Premiere", "Siddhartha Nagar, Bhairahawa, Nepal",
                        27.7100, 83.4600, "+977-71-600000", "info@bodhiredsun.com",
                        "www.bodhiredsun.com", 75,
                        "Grand hotel with standard and deluxe rooms. Features casino, swimming pool, fitness center, in-house restaurant serving international and authentic Nepalese, Chinese, Indian, and continental dishes."),

                createHotel("Dreamland Gold Resort", "Tilottama-5, Manigram, Rupandehi, Nepal",
                        27.7300, 83.4800, "+977-71-610000", "info@dreamlandgold.com",
                        "www.dreamlandgold.com", 90,
                        "Resort offering deluxe, super deluxe, and suite rooms. Features casino, swimming pool, gym, conference hall, variety of cuisines, live bands, cultural shows, folk dances, cycling, and badminton."),

                createHotel("Hotel Palm International", "Milanchowk, Butwal, Nepal",
                        27.7030, 83.4530, "+977-71-620000", "info@palmintl.com",
                        "www.palmintl.com", 35,
                        "Comfortable hotel with terrace, free private parking, bar, and family-friendly accommodations. Perfect for weekend getaways and business travelers."));

        for (Hotel hotel : hotels) {
            if (!hotelRepository.existsByName(hotel.getName())) {
                hotelRepository.save(hotel);
                log.info("Created hotel: {}", hotel.getName());
            }
        }
    }

    private Hotel createHotel(String name, String address, Double latitude, Double longitude,
            String contact, String email, String website, Integer roomsCount, String description) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setAddress(address);
        hotel.setLatitude(latitude);
        hotel.setLongitude(longitude);
        hotel.setContact(contact);
        hotel.setEmail(email);
        hotel.setWebsite(website);
        hotel.setRoomsCount(roomsCount);
        hotel.setDescription(description);
        hotel.setImageUrl("https://res.cloudinary.com/dmkzxeqf0/image/upload/v1756485178/t3zyn3zv1ujfmrhulf21.jpg");
        return hotel;
    }

    private void seedRooms() {
        log.info("Seeding rooms for all hotels...");

        List<Hotel> hotels = hotelRepository.findAll();
        String[] roomTypes = { "Standard", "Deluxe", "Suite", "Presidential", "Family", "Executive" };
        String[] bedTypes = { "Single", "Double", "Queen", "King", "Twin", "California King" };
        String[] categories = { "Economy", "Standard", "Premium", "Luxury", "Presidential" };

        for (Hotel hotel : hotels) {
            int roomCount = hotel.getRoomsCount();
            Random random = new Random();

            for (int i = 1; i <= roomCount; i++) {
                Room room = new Room();
                room.setRoomNumber(i);
                room.setRoomType(roomTypes[random.nextInt(roomTypes.length)]);
                room.setBedType(bedTypes[random.nextInt(bedTypes.length)]);
                room.setRoomCategory(categories[random.nextInt(categories.length)]);
                room.setDescription(generateRoomDescription(room.getRoomType(), room.getBedType()));
                room.setRoomPrice(calculateRoomPrice(room.getRoomType(), room.getRoomCategory()));
                room.setAmenities(generateAmenities(room.getRoomType()));
                room.setPhotoUrl(
                        "https://res.cloudinary.com/dmkzxeqf0/image/upload/v1755531904/geutrlrwckdgyhuvfbyq.jpg");
                room.setHotel(hotel);

                roomRepository.save(room);
            }
            log.info("Created {} rooms for {}", roomCount, hotel.getName());
        }
    }

    private String generateRoomDescription(String roomType, String bedType) {
        return String.format("Comfortable %s room with %s bed, modern amenities, and beautiful views of Butwal. " +
                "Perfect for both business and leisure travelers visiting Nepal.", roomType, bedType);
    }

    private BigDecimal calculateRoomPrice(String roomType, String category) {
        Random random = new Random();
        BigDecimal basePrice = BigDecimal.valueOf(2500); // Base price in Nepalese Rupees

        // Adjust price based on room type
        switch (roomType) {
            case "Standard":
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.0));
                break;
            case "Deluxe":
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.5));
                break;
            case "Suite":
                basePrice = basePrice.multiply(BigDecimal.valueOf(2.0));
                break;
            case "Presidential":
                basePrice = basePrice.multiply(BigDecimal.valueOf(3.0));
                break;
            case "Family":
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.3));
                break;
            case "Executive":
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.8));
                break;
        }

        // Adjust price based on category
        switch (category) {
            case "Economy":
                basePrice = basePrice.multiply(BigDecimal.valueOf(0.8));
                break;
            case "Standard":
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.0));
                break;
            case "Premium":
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.3));
                break;
            case "Luxury":
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.8));
                break;
            case "Presidential":
                basePrice = basePrice.multiply(BigDecimal.valueOf(2.5));
                break;
        }

        // Add some randomness
        double randomFactor = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
        return basePrice.multiply(BigDecimal.valueOf(randomFactor)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private List<String> generateAmenities(String roomType) {
        List<String> baseAmenities = Arrays.asList("Free WiFi", "Air Conditioning", "TV", "Mini Bar", "Safe");
        List<String> amenities = new ArrayList<>(baseAmenities);

        Random random = new Random();

        if (roomType.equals("Deluxe") || roomType.equals("Suite")) {
            amenities.addAll(Arrays.asList("Balcony", "Coffee Machine", "Work Desk"));
        }

        if (roomType.equals("Suite") || roomType.equals("Presidential")) {
            amenities.addAll(Arrays.asList("Living Area", "Kitchenette", "Jacuzzi", "Room Service"));
        }

        if (roomType.equals("Presidential")) {
            amenities.addAll(Arrays.asList("Butler Service", "Private Pool", "Helipad Access"));
        }

        // Shuffle and return 5-8 amenities
        Collections.shuffle(amenities);
        return amenities.subList(0, Math.min(amenities.size(), 5 + random.nextInt(4)));
    }

    private void seedUsers() {
        log.info("Seeding users...");

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        List<User> users = Arrays.asList(
                createUser("Admin", "Admin", "admin@gmail.com", "admin123", adminRole),
                createUser("Sagar", "Shrestha", "sagar@gmail.com", "password123", userRole),
                createUser("Priya", "Gurung", "priya@gmail.com", "password123", userRole),
                createUser("Rajesh", "Tamang", "rajesh@gmail.com", "password123", userRole),
                createUser("Sunita", "Thapa", "sunita@gmail.com", "password123", userRole),
                createUser("Bikash", "Pandey", "bikash@gmail.com", "password123", userRole),
                createUser("Anita", "Maharjan", "anita@gmail.com", "password123", userRole),
                createUser("Niraj", "Shakya", "niraj@gmail.com", "password123", userRole),
                createUser("Pooja", "Bhattarai", "pooja@gmail.com", "password123", userRole),
                createUser("Suresh", "Karki", "suresh@gmail.com", "password123", userRole),
                createUser("Rita", "Limbu", "rita@gmail.com", "password123", userRole));

        for (User user : users) {
            userRepository.save(user);
            log.info("Created user: {}", user.getEmail());
        }
    }

    private User createUser(String firstName, String lastName, String email, String password, Role role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setFullName(firstName + " " + lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(Arrays.asList(role)));
        return user;
    }

    private void seedBookings() {
        log.info("Seeding sample bookings...");

        List<User> users = userRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        BookingStatus[] statuses = BookingStatus.values();

        Random random = new Random();
        LocalDate today = LocalDate.now();

        // Create 50-100 sample bookings
        int bookingCount = 50 + random.nextInt(51);

        for (int i = 0; i < bookingCount; i++) {
            User user = users.get(random.nextInt(users.size()));
            Room room = rooms.get(random.nextInt(rooms.size()));

            // Generate random dates (past, present, and future)
            LocalDate checkIn = today.plusDays(random.nextInt(60) - 30); // -30 to +30 days
            LocalDate checkOut = checkIn.plusDays(1 + random.nextInt(7)); // 1-7 nights

            BookedRoom booking = new BookedRoom();
            booking.setUser(user);
            booking.setRoom(room);
            booking.setCheckInDate(checkIn);
            booking.setCheckOutDate(checkOut);
            booking.setGuestFullName(user.getFullName());
            booking.setGuestEmail(user.getEmail());
            booking.setNumOfAdults(1 + random.nextInt(4)); // 1-4 adults
            booking.setNumOfChildren(random.nextInt(3)); // 0-2 children
            booking.setPhoneNumber("+977-71-" + String.format("%06d", random.nextInt(1000000)));
            booking.setStatus(statuses[random.nextInt(statuses.length)]);

            // Set pricing
            booking.setBasePricePerNight(room.getRoomPrice());
            booking.setFinalPricePerNight(room.getRoomPrice());
            booking.setNumberOfNights((int) ChronoUnit.DAYS.between(checkIn, checkOut));
            booking.calculateTotalAmount();

            // Generate booking confirmation code
            String bookingCode = String.format("BK%08d", random.nextInt(100000000));
            booking.setBookingConfirmationCode(bookingCode);

            bookingRepository.save(booking);
        }

        log.info("Created {} sample bookings", bookingCount);
    }
}
