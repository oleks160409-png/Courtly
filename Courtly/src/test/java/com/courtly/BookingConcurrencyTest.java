package com.courtly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingConcurrencyTest {
    @Autowired private BookingService service;
    @Autowired private BookingRepository repository;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void clearBookings() {
        repository.deleteAll();
    }

    @Test
    void concurrentRequestsCreateExactlyOneBooking() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> attempt = () -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting for concurrent requests.");
                }
                try {
                    service.createBooking(1, LocalDate.of(2026, 11, 12),
                            LocalTime.of(10, 0), LocalTime.of(11, 0));
                    return true;
                } catch (BookingConflictException exception) {
                    return false;
                }
            };
            Future<Boolean> first = executor.submit(attempt);
            Future<Boolean> second = executor.submit(attempt);
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            boolean firstCreated = first.get(10, TimeUnit.SECONDS);
            boolean secondCreated = second.get(10, TimeUnit.SECONDS);
            assertNotEquals(firstCreated, secondCreated);
            assertEquals(1, repository.count());
        } finally {
            start.countDown();
        }
    }

    @Test
    void databaseRejectsOverlapEvenWhenServiceIsBypassed() {
        service.createBooking(1, LocalDate.of(2026, 11, 12),
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO bookings(court_id, booking_date, start_time, end_time) "
                        + "VALUES (1, DATE '2026-11-12', TIME '10:30', TIME '11:30')"));
        assertEquals(1, repository.count());
    }
}
