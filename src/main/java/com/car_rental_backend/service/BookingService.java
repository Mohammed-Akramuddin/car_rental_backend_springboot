package com.car_rental_backend.service;

import com.car_rental_backend.dto.request.BookingRequest;
import com.car_rental_backend.dto.response.BookingDTO;
import com.car_rental_backend.dto.response.PagedResponse;
import com.car_rental_backend.entity.Booking;
import com.car_rental_backend.entity.Car;
import com.car_rental_backend.entity.User;
import com.car_rental_backend.enums.BookingStatus;
import com.car_rental_backend.enums.CarStatus;
import com.car_rental_backend.exception.BadRequestException;
import com.car_rental_backend.exception.ConflictException;
import com.car_rental_backend.exception.ResourceNotFoundException;
import com.car_rental_backend.exception.UnauthorizedException;
import com.car_rental_backend.repository.BookingRepository;
import com.car_rental_backend.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Booking Service.
 * Handles booking creation, management, and validation.
 * Implements double-booking prevention.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserService userService;

    /**
     * Create a new booking (Customer)
     */
    @Transactional
    public BookingDTO createBooking(BookingRequest request) {
        User user = userService.getAuthenticatedUser();
        log.info("Creating booking for user: {} for car: {}", user.getEmail(), request.getCarId());

        // Get the car
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car not found with ID: " + request.getCarId()));

        // Validate car is available for booking
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new BadRequestException("Car is not available for booking. Status: " + car.getStatus());
        }

        // Validate date range
        validateDateRange(request.getStartDate(), request.getEndDate());

        // Check for conflicting bookings (double booking prevention)
        if (bookingRepository.existsConflictingBooking(
                car.getId(), request.getStartDate(), request.getEndDate())) {
            throw new ConflictException(
                    "Car is already booked for the selected dates. Please choose different dates.");
        }

        // Generate unique booking reference
        String bookingReference = generateBookingReference();

        // Create booking
        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .user(user)
                .car(car)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .pricePerDay(car.getPricePerDay())
                .notes(request.getNotes())
                .status(BookingStatus.PENDING)
                .build();

        // Calculate fields (totalDays, totalPrice)
        booking.calculateFields();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: {}", savedBooking.getBookingReference());

        return mapToDTO(savedBooking);
    }

    /**
     * Get booking by ID
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        User user = userService.getAuthenticatedUser();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        // Check authorization - user can only see their own bookings (unless admin)
        if (!isAdmin(user) && !booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to view this booking");
        }

        return mapToDTO(booking);
    }

    /**
     * Get booking by reference
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingByReference(String reference) {
        User user = userService.getAuthenticatedUser();
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with reference: " + reference));

        if (!isAdmin(user) && !booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to view this booking");
        }

        return mapToDTO(booking);
    }

    /**
     * Get current user's bookings
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getMyBookings() {
        User user = userService.getAuthenticatedUser();
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return bookings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get current user's bookings with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<BookingDTO> getMyBookingsPaged(Pageable pageable) {
        User user = userService.getAuthenticatedUser();
        Page<Booking> bookings = bookingRepository.findByUserId(user.getId(), pageable);
        return PagedResponse.from(bookings,
                bookings.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Get all bookings (Admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<BookingDTO> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PagedResponse.from(bookings,
                bookings.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Get bookings by status (Admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<BookingDTO> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByStatus(status, pageable);
        return PagedResponse.from(bookings,
                bookings.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Search bookings (Admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<BookingDTO> searchBookings(String searchTerm, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.searchBookings(searchTerm, pageable);
        return PagedResponse.from(bookings,
                bookings.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Confirm booking (Admin only)
     */
    @Transactional
    public BookingDTO confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending bookings can be confirmed. Current status: " + booking.getStatus());
        }

        // Recheck for conflicts (in case of concurrent bookings)
        if (bookingRepository.existsConflictingBookingExcluding(
                booking.getCar().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getId())) {
            throw new ConflictException("Conflicting booking exists. Cannot confirm.");
        }

        booking.confirm();
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking {} confirmed", booking.getBookingReference());

        return mapToDTO(updatedBooking);
    }

    /**
     * Cancel booking
     */
    @Transactional
    public BookingDTO cancelBooking(Long id, String reason) {
        User user = userService.getAuthenticatedUser();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        // Authorization check
        if (!isAdmin(user) && !booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to cancel this booking");
        }

        if (!booking.canBeCancelled()) {
            throw new BadRequestException(
                    "Booking cannot be cancelled. Current status: " + booking.getStatus());
        }

        // Additional check: cannot cancel if rental has already started
        if (booking.getStartDate().isBefore(LocalDate.now()) || 
            booking.getStartDate().isEqual(LocalDate.now())) {
            if (!isAdmin(user)) {
                throw new BadRequestException(
                    "Cannot cancel booking after rental start date. Contact support.");
            }
        }

        booking.cancel(reason != null ? reason : "Cancelled by user");
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking {} cancelled by {}", booking.getBookingReference(), user.getEmail());

        return mapToDTO(updatedBooking);
    }

    /**
     * Complete booking (Admin only)
     */
    @Transactional
    public BookingDTO completeBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Only confirmed bookings can be completed. Current status: " + booking.getStatus());
        }

        booking.complete();
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking {} completed", booking.getBookingReference());

        return mapToDTO(updatedBooking);
    }

    /**
     * Get bookings for a specific car (Admin only)
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsForCar(Long carId) {
        List<Booking> bookings = bookingRepository.findByCarIdOrderByStartDateDesc(carId);
        return bookings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming bookings (Admin only)
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getUpcomingBookings() {
        List<Booking> bookings = bookingRepository.findUpcomingBookings(LocalDate.now());
        return bookings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // Helper Methods
    // ============================================

    /**
     * Generate unique booking reference
     */
    private String generateBookingReference() {
        String reference;
        do {
            reference = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.existsByBookingReference(reference));
        return reference;
    }

    /**
     * Validate date range
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after or equal to start date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }
    }

    /**
     * Check if user is admin
     */
    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
    }

    /**
     * Map Booking entity to BookingDTO
     */
    private BookingDTO mapToDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUser().getId())
                .userEmail(booking.getUser().getEmail())
                .userFullName(booking.getUser().getFullName())
                .carId(booking.getCar().getId())
                .carBrand(booking.getCar().getBrand())
                .carModel(booking.getCar().getModel())
                .carFullName(booking.getCar().getFullName())
                .carImageUrl(booking.getCar().getImageUrl())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .pickupLocation(booking.getPickupLocation())
                .dropoffLocation(booking.getDropoffLocation())
                .pricePerDay(booking.getPricePerDay())
                .totalDays(booking.getTotalDays())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .notes(booking.getNotes())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
