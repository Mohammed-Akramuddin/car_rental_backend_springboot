package com.car_rental_backend.enums;

/**
 * Enumeration representing the lifecycle status of a booking.
 */
public enum BookingStatus {
    PENDING,    // Booking created, awaiting confirmation
    CONFIRMED,  // Booking confirmed and active
    CANCELLED,  // Booking was cancelled
    COMPLETED   // Rental period has ended
}
