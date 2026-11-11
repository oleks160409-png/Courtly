import java.time.LocalDate;
import java.time.LocalTime;

public class Booking {
    private final int id;
    private final Court court;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public Booking(int id, Court court, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.court = court;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
