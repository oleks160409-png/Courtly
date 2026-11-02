import java.util.Scanner;

public class Main {
    public static final int OPENING_HOUR = 8;
    public static final int CLOSING_HOUR = 21;
    public static void main(String[] args) {
        boolean isRunning = true;
        String[] courtHours = new String[CLOSING_HOUR - OPENING_HOUR + 1];
        for (int index = 0; index < courtHours.length; index++) {
            courtHours[index] = "available";
        }
        Scanner scanner = new Scanner(System.in);
        while (isRunning == true) {
            printMenu();
            System.out.println("Enter the option number: ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> System.out.println("Opening hours: 08:00–22:00");
                    case 2 -> printCourtAvailability(courtHours);
                    case 3 -> createBooking(courtHours, scanner);
                    case 4 -> cancelBooking(courtHours, scanner);
                    case 5 -> showMyBookings(courtHours);
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
        System.out.println("0. Exit");
    }

    public static void printCourtAvailability(String[] courtHours) {
        for (int index = 0; index < courtHours.length; index++) {
            int hour = OPENING_HOUR + index;
            if (hour < 10) {
                System.out.println("0" + hour + ":00" + " - " + courtHours[index]);
            } else {
                System.out.println(hour + ":00" + " - " + courtHours[index]);
            }
        }
    }

    public static void createBooking(String[] courtHours, Scanner scanner) {
        System.out.println("Enter time: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Please enter a number.");
            scanner.next();
            return;
        }
        int timeChoice = scanner.nextInt();
        if (timeChoice < OPENING_HOUR || timeChoice > CLOSING_HOUR) {
            System.out.println("Invalid time");
            return;
        } else {
            int index = timeChoice - OPENING_HOUR;
            if (courtHours[index].equals("available")) {
                courtHours[index] = "booked";
                System.out.println("Booking created for " + timeChoice + ":00");
            } else {
                System.out.println("This hour is already booked.");
            }
        }
    }

    public static void cancelBooking(String[] courtHours, Scanner scanner) {
        System.out.println("Enter time: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Please enter a number.");
            scanner.next();
            return;
        }
        int timeChoice = scanner.nextInt();
        if (timeChoice < OPENING_HOUR || timeChoice > CLOSING_HOUR) {
            System.out.println("Invalid time");
            return;
        } else {
            int index = timeChoice - OPENING_HOUR;
            if (courtHours[index].equals("booked")) {
                courtHours[index] = "available";
                System.out.println("Booking cancelled for " + timeChoice + ":00");
            } else {
                System.out.println("This hour is not booked.");
            }
        }
    }
    public static void showMyBookings(String[] courtHours) {
        int bookingCount = 0;
        for (int index = 0; index < courtHours.length; index++) {
            if (courtHours[index].equals("booked")) {
                int hour = OPENING_HOUR + index;
                bookingCount++;
                System.out.println("You have a booking for " + hour + ":00");
            }
        }
        if (bookingCount == 0) {
            System.out.println("You have no bookings. ");
        } else if (bookingCount == 1) {
            System.out.println("You have " + bookingCount + " " + "booking");
        } else {
            System.out.println("You have " + bookingCount + " " + "bookings");
        }
    }
}
