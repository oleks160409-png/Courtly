package com.courtly;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
