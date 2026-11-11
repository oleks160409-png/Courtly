import java.time.LocalDate;

public class Booking {
    private final int id;
    private final Court court;
    private final LocalDate date;
    private final int startHour;
    private final int endHour;

    public Booking(int id, Court court, LocalDate date, int startHour, int endHour) {
        this.id = id;
        this.court = court;
        this.date = date;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public int getId() {
        return id;
    }

    public Court getCourt() {
        return court;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }
}
