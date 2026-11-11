package com.courtly;

public class BookingConflictException extends IllegalArgumentException {
    public BookingConflictException() {
        super("Selected period is not available.");
    }
}
