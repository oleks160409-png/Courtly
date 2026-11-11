package com.courtly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BookingServiceUnitTest {
    private BookingRepository bookings;
    private CourtRepository courts;
    private BookingService service;

    @BeforeEach
    void setUp() {
        bookings = mock(BookingRepository.class);
        courts = mock(CourtRepository.class);
        service = new BookingService(bookings, courts);
        when(courts.findById(1)).thenReturn(Optional.of(new Court(1, "Tennis Court")));
    }

    @Test
    void rejectsMissingDateBeforeAccessingBookings() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking(1, null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        verifyNoInteractions(bookings);
    }

    @Test
    void rejectsMissingTimeBeforeAccessingBookings() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking(1, LocalDate.of(2026, 11, 12), null, LocalTime.of(11, 0)));
        verifyNoInteractions(bookings);
    }

    @Test
    void rejectsUnknownCourtBeforeAccessingBookings() {
        when(courts.findById(999)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createBooking(999,
                LocalDate.of(2026, 11, 12), LocalTime.of(10, 0), LocalTime.of(11, 0)));
        verifyNoInteractions(bookings);
    }
}
