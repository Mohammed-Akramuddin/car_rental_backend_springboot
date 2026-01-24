package com.car_rental_backend.enums;

/**
 * Enumeration representing user account status.
 * Used for account management and access control.
 */
public enum AccountStatus {
    ACTIVE,              // Account is active and can use the system
    PENDING_ACTIVATION,  // Registered but email not yet verified
    DISABLED,            // Account is disabled by admin
    SUSPENDED            // Account is temporarily suspended
}
