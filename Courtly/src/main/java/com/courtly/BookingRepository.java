package com.courtly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findAllByOrderByDateAscStartTimeAscIdAsc();

    @Query("select count(b) > 0 from Booking b where b.court.id = :courtId and b.date = :date "
            + "and b.startTime < :endTime and b.endTime > :startTime")
    boolean overlaps(int courtId, LocalDate date, LocalTime startTime, LocalTime endTime);

    @Query("select b from Booking b where b.court.id = :courtId and b.date = :date "
            + "and b.startTime <= :time and b.endTime > :time")
    List<Booking> findForTime(int courtId, LocalDate date, LocalTime time);
}
