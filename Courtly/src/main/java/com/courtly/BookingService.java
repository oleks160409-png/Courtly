package com.courtly;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingService {
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    public static final int SLOT_MINUTES = 30;

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;

    public BookingService(BookingRepository bookingRepository, CourtRepository courtRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
    }

    public ArrayList<Court> getCourts() {
        return new ArrayList<>(courtRepository.findAll(Sort.by("id")));
    }

    public Court getCourtById(int courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found."));
    }

    @Transactional
    public Booking createBooking(int courtId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Court court = getCourtById(courtId);
        requireDate(date);
        validateTime(startTime);
        validateTime(endTime);
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (bookingRepository.overlaps(courtId, date, startTime, endTime)) {
            throw new BookingConflictException();
        }

        try {
            return bookingRepository.saveAndFlush(new Booking(court, date, startTime, endTime));
        } catch (DataIntegrityViolationException exception) {
            // Concurrent requests can pass the initial check; the database rejects the overlap.
            Throwable cause = exception;
            while (cause != null) {
                if (cause instanceof SQLException sql && "23P01".equals(sql.getSQLState())) {
                    throw new BookingConflictException();
                }
                cause = cause.getCause();
            }
            throw exception;
        }
    }

    @Transactional
    public boolean cancelBooking(int bookingId) {
        validateBookingId(bookingId);
        return bookingRepository.findById(bookingId).map(booking -> {
            bookingRepository.delete(booking);
            return true;
        }).orElse(false);
    }

    public Booking getBookingForTime(int courtId, LocalDate date, LocalTime time) {
        getCourtById(courtId);
        requireDate(date);
        validateTime(time);
        if (!time.isBefore(CLOSING_TIME)) {
            throw new IllegalArgumentException("Slot must start before closing time.");
        }
        List<Booking> bookings = bookingRepository.findForTime(courtId, date, time);
        return bookings.isEmpty() ? null : bookings.get(0);
    }

    public Booking getBookingById(int id) {
        validateBookingId(id);
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
    }

    public ArrayList<Booking> getBookings() {
        return new ArrayList<>(bookingRepository.findAllByOrderByDateAscStartTimeAscIdAsc());
    }

    private void validateBookingId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid booking ID.");
        }
    }

    private void requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
    }

    private void validateTime(LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("Time is required.");
        }
        if (time.isBefore(OPENING_TIME) || time.isAfter(CLOSING_TIME)) {
            throw new IllegalArgumentException("Time must be between 08:00 and 22:00.");
        }
        if (time.getMinute() % SLOT_MINUTES != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw new IllegalArgumentException("Time must use 30-minute steps.");
        }
    }
}
