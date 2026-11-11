import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class BookingService {
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    public static final int SLOT_MINUTES = 30;

    private final ArrayList<Booking> bookings = new ArrayList<>();
    private final ArrayList<Court> courts = new ArrayList<>();
    private int nextBookingId = 1;

    public BookingService() {
        courts.add(new Court(1, "Tennis Court"));
        courts.add(new Court(2, "Basketball Court"));
    }

    public ArrayList<Court> getCourts() {
        return new ArrayList<>(courts);
    }

    public Court getCourtById(int courtId) {
        for (Court court : courts) {
            if (court.getId() == courtId) {
                return court;
            }
        }
        throw new IllegalArgumentException("Court not found.");
    }

    public Booking createBooking(int courtId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Court court = getCourtById(courtId);
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        validateTime(startTime);
        validateTime(endTime);
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        for (Booking booking : bookings) {
            if (booking.getCourt().getId() == courtId
                    && booking.getDate().equals(date)
                    && startTime.isBefore(booking.getEndTime())
                    && endTime.isAfter(booking.getStartTime())) {
                throw new IllegalArgumentException("Selected period is not available.");
            }
        }

        Booking booking = new Booking(nextBookingId, court, date, startTime, endTime);
        int position = 0;
        while (position < bookings.size()
                && (bookings.get(position).getDate().isBefore(date)
                || (bookings.get(position).getDate().equals(date)
                && bookings.get(position).getStartTime().isBefore(startTime)))) {
            position++;
        }
        bookings.add(position, booking);
        nextBookingId++;
        return booking;
    }

    public boolean cancelBooking(int bookingId) {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Invalid booking ID.");
        }
        for (int index = 0; index < bookings.size(); index++) {
            if (bookings.get(index).getId() == bookingId) {
                bookings.remove(index);
                return true;
            }
        }
        return false;
    }

    public Booking getBookingForTime(int courtId, LocalDate date, LocalTime time) {
        getCourtById(courtId);
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        validateTime(time);
        if (!time.isBefore(CLOSING_TIME)) {
            throw new IllegalArgumentException("Slot must start before closing time.");
        }
        for (Booking booking : bookings) {
            if (booking.getCourt().getId() == courtId
                    && booking.getDate().equals(date)
                    && !time.isBefore(booking.getStartTime()) && time.isBefore(booking.getEndTime())) {
                return booking;
            }
        }
        return null;
    }

    public ArrayList<Booking> getBookings() {
        return new ArrayList<>(bookings);
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
