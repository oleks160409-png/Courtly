package com.courtly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BookingServiceTest {
    @Test
    void allowsSameTimeOnDifferentCourts() {
        BookingService service = new BookingService();
        LocalDate date = LocalDate.of(2026, 11, 12);
        Booking first = service.createBooking(1, date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking second = service.createBooking(2, date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(2, service.getBookings().size());
        assertSame(first, service.getBookingForTime(1, date, LocalTime.of(10, 0)));
        assertSame(second, service.getBookingForTime(2, date, LocalTime.of(10, 0)));
    }

    @Test
    void allowsSameTimeOnDifferentDates() {
        BookingService service = new BookingService();
        LocalDate firstDate = LocalDate.of(2026, 11, 12);
        LocalDate secondDate = firstDate.plusDays(1);
        Booking first = service.createBooking(1, firstDate, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking second = service.createBooking(1, secondDate, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(2, service.getBookings().size());
        assertSame(first, service.getBookingForTime(1, firstDate, LocalTime.of(10, 0)));
        assertSame(second, service.getBookingForTime(1, secondDate, LocalTime.of(10, 0)));
    }

    @Test
    void cancellationFreesEntirePeriodAndPreservesOtherBooking() {
        BookingService service = new BookingService();
        LocalDate date = LocalDate.of(2026, 11, 12);
        Booking cancelled = service.createBooking(1, date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking retained = service.createBooking(2, date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertTrue(service.cancelBooking(cancelled.getId()));
        assertFalse(service.cancelBooking(cancelled.getId()));
        assertEquals(1, service.getBookings().size());
        assertSame(retained, service.getBookings().get(0));
        assertNull(service.getBookingForTime(1, date, LocalTime.of(10, 0)));
        assertNull(service.getBookingForTime(1, date, LocalTime.of(10, 30)));
        assertSame(retained, service.getBookingForTime(2, date, LocalTime.of(10, 0)));

        Booking replacement = service.createBooking(1, date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertTrue(replacement.getId() > retained.getId());
    }

    @Test
    void allowsBookingFromOpeningToClosingTime() {
        BookingService service = new BookingService();
        LocalDate date = LocalDate.of(2026, 11, 12);
        Booking booking = service.createBooking(1, date, LocalTime.of(8, 0), LocalTime.of(22, 0));

        assertEquals(LocalTime.of(8, 0), booking.getStartTime());
        assertEquals(LocalTime.of(22, 0), booking.getEndTime());
        assertSame(booking, service.getBookingForTime(1, date, LocalTime.of(8, 0)));
        assertSame(booking, service.getBookingForTime(1, date, LocalTime.of(21, 30)));
    }

    @ParameterizedTest
    @CsvSource({
            "07:30, 08:00",
            "21:30, 22:30",
            "10:00, 10:00",
            "11:00, 10:00",
            "10:15, 11:00",
            "10:00:01, 11:00"
    })
    void rejectsInvalidTimesWithoutCreatingBooking(String start, String end) {
        BookingService service = new BookingService();
        LocalDate date = LocalDate.of(2026, 11, 12);

        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking(1, date, LocalTime.parse(start), LocalTime.parse(end)));

        assertTrue(service.getBookings().isEmpty());
        Booking valid = service.createBooking(1, date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertEquals(1, valid.getId());
    }

    @Test
    void allowsAdjacentBookingsAndAssignsBoundaryToSecondBooking() {
        BookingService service = new BookingService();
        LocalDate date = LocalDate.of(2026, 11, 12);

        Booking first = service.createBooking(1, date, LocalTime.of(10, 0), LocalTime.of(10, 30));
        Booking second = service.createBooking(1, date, LocalTime.of(10, 30), LocalTime.of(11, 0));

        assertEquals(2, service.getBookings().size());
        assertSame(first, service.getBookingForTime(1, date, LocalTime.of(10, 0)));
        assertSame(second, service.getBookingForTime(1, date, LocalTime.of(10, 30)));
        assertNull(service.getBookingForTime(1, date, LocalTime.of(11, 0)));
    }

    @Test
    void rejectsOverlappingBookingWithoutChangingExistingBookings() {
        BookingService service = new BookingService();
        LocalDate date = LocalDate.of(2026, 11, 12);
        Booking original = service.createBooking(1, date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertThrows(IllegalArgumentException.class, () ->
                service.createBooking(1, date, LocalTime.of(10, 30), LocalTime.of(11, 30)));

        assertEquals(1, service.getBookings().size());
        assertSame(original, service.getBookings().get(0));
        assertSame(original, service.getBookingForTime(1, date, LocalTime.of(10, 30)));
        assertNull(service.getBookingForTime(1, date, LocalTime.of(11, 0)));
    }
}
