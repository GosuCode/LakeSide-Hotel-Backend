package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.exception.InvalidBookingRequestException;
import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.BookedRoom;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.repository.BookingRepository;
import com.dailycodework.lakesidehotel.response.DynamicPricingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final IRoomService roomService;
    private final IDynamicPricingService dynamicPricingService;

    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public List<BookedRoom> getBookingsByUserEmail(String email) {
        return bookingRepository.findByGuestEmail(email);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
            throw new InvalidBookingRequestException("Check-in date must come before check-out date");
        }

        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);

        if (roomIsAvailable) {
            // Calculate dynamic pricing and store it
            calculateAndStorePricing(room, bookingRequest);

            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
        } else {
            throw new InvalidBookingRequestException("Sorry, This room is not available for the selected dates;");
        }
        return bookingRequest.getBookingConfirmationCode();
    }

    private void calculateAndStorePricing(Room room, BookedRoom bookingRequest) {
        try {
            // Get dynamic pricing for the booking dates
            DynamicPricingResponse pricing = dynamicPricingService.calculatePrice(
                    room.getId(),
                    bookingRequest.getCheckInDate(),
                    bookingRequest.getCheckOutDate());

            // Store the pricing information
            bookingRequest.setBasePricePerNight(room.getRoomPrice());
            bookingRequest.setFinalPricePerNight(pricing.getFinalPrice());

            // Calculate number of nights
            int nights = bookingRequest.getCheckOutDate().getDayOfYear() -
                    bookingRequest.getCheckInDate().getDayOfYear();
            if (nights == 0)
                nights = 1; // Minimum 1 night

            bookingRequest.setNumberOfNights(nights);
            bookingRequest.setTotalAmount(pricing.getFinalPrice().multiply(BigDecimal.valueOf(nights)));

            // Store pricing adjustments as JSON string
            if (pricing.getAdjustments() != null && !pricing.getAdjustments().isEmpty()) {
                String adjustmentsJson = convertAdjustmentsToJson(pricing.getAdjustments());
                bookingRequest.setPricingAdjustments(adjustmentsJson);
            }

        } catch (Exception e) {
            // Fallback to base pricing if dynamic pricing fails
            bookingRequest.setBasePricePerNight(room.getRoomPrice());
            bookingRequest.setFinalPricePerNight(room.getRoomPrice());
            bookingRequest.setNumberOfNights(1);
            bookingRequest.setTotalAmount(room.getRoomPrice());
        }
    }

    private String convertAdjustmentsToJson(
            List<com.dailycodework.lakesidehotel.response.PricingAdjustment> adjustments) {
        // Simple JSON conversion - you might want to use Jackson ObjectMapper for
        // production
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < adjustments.size(); i++) {
            com.dailycodework.lakesidehotel.response.PricingAdjustment adj = adjustments.get(i);
            json.append("{");
            json.append("\"name\":\"").append(adj.getName()).append("\",");
            json.append("\"percentage\":").append(adj.getPercentage()).append(",");
            json.append("\"amount\":").append(adj.getAmount()).append(",");
            json.append("\"description\":\"").append(adj.getDescription()).append("\"");
            json.append("}");
            if (i < adjustments.size() - 1)
                json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No booking found with booking code :" + confirmationCode));
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking -> bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
                        || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                        || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                                && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))

                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))

                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate())));
    }
}
