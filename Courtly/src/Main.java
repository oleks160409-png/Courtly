import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean isRunning = true;
        BookingService bookingService = new BookingService();
        Scanner scanner = new Scanner(System.in);
        while (isRunning == true) {
            printMenu();
            System.out.println("Enter the option number: ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> System.out.printf("Opening hours: %02d:00–%02d:00%n",
                            BookingService.OPENING_HOUR, BookingService.CLOSING_HOUR);
                    case 2 -> printCourtAvailability(bookingService, scanner);
                    case 3 -> createBooking(bookingService, scanner);
                    case 4 -> cancelBooking(bookingService, scanner);
                    case 5 -> showMyBookings(bookingService);
                    case 6 -> showCourts(bookingService);
                    case 0 -> isRunning = false;
                    default -> System.out.println("Unknown option.");
                }
            } else {
                System.out.println("Please enter a number.");
                scanner.next();
            }
        }
        System.out.println("Thank you, Goodbye!");
    }

    public static void printMenu() {
        System.out.println("=== Courtly Booking System ===");
        System.out.println("1. Show opening hours");
        System.out.println("2. Show court availability");
        System.out.println("3. Create booking");
        System.out.println("4. Cancel booking");
        System.out.println("5. Show my bookings");
        System.out.println("6. Show courts");
        System.out.println("0. Exit");
    }

    public static void showCourts(BookingService bookingService) {
        System.out.println("Available courts:");
        for (Court court : bookingService.getCourts()) {
            System.out.println(court.getId() + ". " + court.getName());
        }
    }

    public static Court readCourt(BookingService bookingService, Scanner scanner) {
        showCourts(bookingService);
        System.out.println("Enter court ID: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Please enter a number.");
            scanner.next();
            return null;
        }
        int courtId = scanner.nextInt();
        try {
            return bookingService.getCourtById(courtId);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }

    public static void printCourtAvailability(BookingService bookingService, Scanner scanner) {
        Court court = readCourt(bookingService, scanner);
        if (court == null) {
            return;
        }
        LocalDate date = readDate(scanner);
        if (date == null) {
            return;
        }
        System.out.println("Availability: " + court.getName() + ", " + date);
        for (int hour = BookingService.OPENING_HOUR; hour < BookingService.CLOSING_HOUR; hour++) {
            Booking booking = bookingService.getBookingForHour(court.getId(), date, hour);
            if (booking == null) {
                System.out.printf("%02d:00 - available%n", hour);
            } else {
                System.out.printf("%02d:00 - booked (#%d)%n", hour, booking.getId());
            }
        }
    }

    public static void createBooking(BookingService bookingService, Scanner scanner) {
        Court court = readCourt(bookingService, scanner);
        if (court == null) {
            return;
        }
        LocalDate date = readDate(scanner);
        if (date == null) {
            return;
        }
        int startHour = readTime(scanner, "Enter start hour: ",
                BookingService.OPENING_HOUR, BookingService.CLOSING_HOUR - 1);
        if (startHour == -1) {
            return;
        }

        int endHour = readTime(scanner, "Enter end hour: ",
                BookingService.OPENING_HOUR + 1, BookingService.CLOSING_HOUR);
        if (endHour == -1) {
            return;
        }
        try {
            Booking booking = bookingService.createBooking(court.getId(), date, startHour, endHour);
            System.out.printf("Booking #%d created: %s, %s, %02d:00–%02d:00%n",
                    booking.getId(), booking.getCourt().getName(), booking.getDate(),
                    booking.getStartHour(), booking.getEndHour());
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static LocalDate readDate(Scanner scanner) {
        System.out.println("Enter date (YYYY-MM-DD): ");
        String input = scanner.next();
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
            return null;
        }
    }

    public static void cancelBooking(BookingService bookingService, Scanner scanner) {
        System.out.println("Enter booking ID to cancel: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Please enter a number.");
            scanner.next();
            return;
        }

        int bookingId = scanner.nextInt();
        try {
            if (bookingService.cancelBooking(bookingId)) {
                System.out.println("Booking #" + bookingId + " cancelled.");
            } else {
                System.out.println("Booking not found.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }
    public static int readTime(Scanner scanner, String prompt, int minHour, int maxHour) {
        System.out.println(prompt);
        if (!scanner.hasNextInt()) {
            System.out.println("Please enter a number.");
            scanner.next();
            return -1;
        }

        int timeChoice = scanner.nextInt();
        if (timeChoice < minHour || timeChoice > maxHour) {
            System.out.println("Invalid time");
            return -1;
        }

        return timeChoice;
    }

    public static void showMyBookings(BookingService bookingService) {
        ArrayList<Booking> bookings = bookingService.getBookings();
        for (Booking booking : bookings) {
            System.out.printf("#%d %s, %s, %02d:00–%02d:00%n",
                    booking.getId(), booking.getCourt().getName(), booking.getDate(),
                    booking.getStartHour(), booking.getEndHour());
        }
        int bookingCount = bookings.size();
        if (bookingCount == 0) {
            System.out.println("You have no bookings. ");
        } else if (bookingCount == 1) {
            System.out.println("You have " + bookingCount + " booking.");
        } else {
            System.out.println("You have " + bookingCount + " bookings.");
        }
    }
}
