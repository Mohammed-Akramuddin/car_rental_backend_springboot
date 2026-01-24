package com.car_rental_backend.enums;

/**
 * Enumeration representing the current status of a car.
 * Controls availability for booking.
 */
public enum CarStatus {
    AVAILABLE,      // Car is available for booking
    BOOKED,         // Car is currently booked/rented
    MAINTENANCE     // Car is under maintenance, not available
}
