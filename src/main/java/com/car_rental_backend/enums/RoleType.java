package com.car_rental_backend.enums;

/**
 * Enumeration of available user roles in the system.
 * Used for role-based access control (RBAC).
 */
public enum RoleType {
    ROLE_ADMIN,     // Full system access - manage cars, users, all bookings
    ROLE_CUSTOMER   // Limited access - book cars, view own bookings
}
