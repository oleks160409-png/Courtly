import java.time.LocalDate;
import java.util.ArrayList;

public class BookingService {
    public static final int OPENING_HOUR = 8;
    public static final int CLOSING_HOUR = 22;

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

    public Booking createBooking(int courtId, LocalDate date, int startHour, int endHour) {
        Court court = getCourtById(courtId);
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (startHour < OPENING_HOUR || startHour >= CLOSING_HOUR
                || endHour <= OPENING_HOUR || endHour > CLOSING_HOUR) {
            throw new IllegalArgumentException("Invalid time");
        }
        if (startHour >= endHour) {
            throw new IllegalArgumentException("Start hour must be before end hour.");
        }

        for (Booking booking : bookings) {
            if (booking.getCourt().getId() == courtId
                    && booking.getDate().equals(date)
                    && startHour < booking.getEndHour() && endHour > booking.getStartHour()) {
                throw new IllegalArgumentException("Selected period is not available.");
            }
        }

        Booking booking = new Booking(nextBookingId, court, date, startHour, endHour);
        int position = 0;
        while (position < bookings.size()
                && (bookings.get(position).getDate().isBefore(date)
                || (bookings.get(position).getDate().equals(date)
                && bookings.get(position).getStartHour() < startHour))) {
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

    public Booking getBookingForHour(int courtId, LocalDate date, int hour) {
        getCourtById(courtId);
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (hour < OPENING_HOUR || hour >= CLOSING_HOUR) {
            throw new IllegalArgumentException("Invalid time");
        }
        for (Booking booking : bookings) {
            if (booking.getCourt().getId() == courtId
                    && booking.getDate().equals(date)
                    && hour >= booking.getStartHour() && hour < booking.getEndHour()) {
                return booking;
            }
        }
        return null;
    }

    public ArrayList<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }
}
