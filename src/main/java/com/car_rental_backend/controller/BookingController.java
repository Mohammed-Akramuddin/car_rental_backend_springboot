package com.car_rental_backend.controller;

import com.car_rental_backend.dto.request.BookingRequest;
import com.car_rental_backend.dto.response.ApiResponse;
import com.car_rental_backend.dto.response.BookingDTO;
import com.car_rental_backend.dto.response.PagedResponse;
import com.car_rental_backend.enums.BookingStatus;
import com.car_rental_backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Booking Controller.
 * Handles car rental booking endpoints.
 * 
 * Customer: Create bookings, view own bookings, cancel own bookings
 * Admin: View all bookings, confirm, complete, cancel any booking
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    // ============================================
    // Customer Endpoints (Authenticated users)
    // ============================================

    /**
     * Create a new booking
     * 
     * POST /api/v1/bookings
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        log.info("Creating booking for car ID: {}", request.getCarId());
        
        BookingDTO booking = bookingService.createBooking(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    /**
     * Get current user's bookings
     * 
     * GET /api/v1/bookings/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getMyBookings() {
        List<BookingDTO> bookings = bookingService.getMyBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get current user's bookings with pagination
     * 
     * GET /api/v1/bookings/my/paged
     */
    @GetMapping("/my/paged")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDTO>>> getMyBookingsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<BookingDTO> bookings = bookingService.getMyBookingsPaged(pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get booking by ID (User can only see their own, Admin can see all)
     * 
     * GET /api/v1/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Get booking by reference
     * 
     * GET /api/v1/bookings/reference/{reference}
     */
    @GetMapping("/reference/{reference}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingByReference(
            @PathVariable String reference) {
        BookingDTO booking = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Cancel booking (User can cancel their own, Admin can cancel any)
     * 
     * POST /api/v1/bookings/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling booking ID: {}", id);
        
        BookingDTO booking = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }

    // ============================================
    // Admin Endpoints
    // ============================================

    /**
     * Get all bookings (Admin only)
     * 
     * GET /api/v1/bookings/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDTO>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PagedResponse<BookingDTO> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get bookings by status (Admin only)
     * 
     * GET /api/v1/bookings/admin/status/{status}
     */
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDTO>>> getBookingsByStatus(
            @PathVariable BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<BookingDTO> bookings = bookingService.getBookingsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Search bookings (Admin only)
     * 
     * GET /api/v1/bookings/admin/search
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<BookingDTO>>> searchBookings(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<BookingDTO> bookings = bookingService.searchBookings(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get bookings for a specific car (Admin only)
     * 
     * GET /api/v1/bookings/admin/car/{carId}
     */
    @GetMapping("/admin/car/{carId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getBookingsForCar(
            @PathVariable Long carId) {
        List<BookingDTO> bookings = bookingService.getBookingsForCar(carId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Get upcoming bookings (Admin only)
     * 
     * GET /api/v1/bookings/admin/upcoming
     */
    @GetMapping("/admin/upcoming")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getUpcomingBookings() {
        List<BookingDTO> bookings = bookingService.getUpcomingBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * Confirm booking (Admin only)
     * 
     * POST /api/v1/bookings/admin/{id}/confirm
     */
    @PostMapping("/admin/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> confirmBooking(@PathVariable Long id) {
        log.info("Confirming booking ID: {}", id);
        
        BookingDTO booking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", booking));
    }

    /**
     * Complete booking (Admin only)
     * 
     * POST /api/v1/bookings/admin/{id}/complete
     */
    @PostMapping("/admin/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> completeBooking(@PathVariable Long id) {
        log.info("Completing booking ID: {}", id);
        
        BookingDTO booking = bookingService.completeBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking completed successfully", booking));
    }
}
