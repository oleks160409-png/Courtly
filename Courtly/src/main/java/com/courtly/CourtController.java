package com.courtly;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CourtController {
    private final BookingService bookingService;

    public CourtController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/courts")
    public List<Court> getCourts() {
        return bookingService.getCourts();
    }

    public record AvailabilitySlot(LocalTime startTime, LocalTime endTime, boolean available, Integer bookingId) {}

    @GetMapping("/courts/{id}/availability")
    public List<AvailabilitySlot> getAvailability(@PathVariable int id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        bookingService.getCourtById(id);
        List<AvailabilitySlot> slots = new ArrayList<>();
        for (LocalTime time = BookingService.OPENING_TIME; time.isBefore(BookingService.CLOSING_TIME);
                time = time.plusMinutes(BookingService.SLOT_MINUTES)) {
            Booking booking = bookingService.getBookingForTime(id, date, time);
            slots.add(new AvailabilitySlot(time, time.plusMinutes(BookingService.SLOT_MINUTES),
                    booking == null, booking == null ? null : booking.getId()));
        }
        return slots;
    }
}
