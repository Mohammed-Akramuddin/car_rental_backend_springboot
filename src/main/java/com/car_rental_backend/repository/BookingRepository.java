package com.car_rental_backend.repository;

import com.car_rental_backend.entity.Booking;
import com.car_rental_backend.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Booking entity.
 * Provides CRUD operations and advanced queries for booking management.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find booking by reference
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Check if booking reference exists
     */
    boolean existsByBookingReference(String bookingReference);

    /**
     * Find bookings by user
     */
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find bookings by user with pagination
     */
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    /**
     * Find bookings by car
     */
    List<Booking> findByCarIdOrderByStartDateDesc(Long carId);

    /**
     * Find bookings by status
     */
    List<Booking> findByStatus(BookingStatus status);

    /**
     * Find bookings by status with pagination
     */
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    /**
     * Find bookings by user and status
     */
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    /**
     * Check for conflicting bookings (double booking prevention)
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
           "b.car.id = :carId AND " +
           "b.status IN ('PENDING', 'CONFIRMED') AND " +
           "b.startDate <= :endDate AND " +
           "b.endDate >= :startDate")
    boolean existsConflictingBooking(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Check for conflicting bookings excluding a specific booking (for updates)
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
           "b.car.id = :carId AND " +
           "b.id != :excludeBookingId AND " +
           "b.status IN ('PENDING', 'CONFIRMED') AND " +
           "b.startDate <= :endDate AND " +
           "b.endDate >= :startDate")
    boolean existsConflictingBookingExcluding(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeBookingId") Long excludeBookingId
    );

    /**
     * Find conflicting bookings
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "b.car.id = :carId AND " +
           "b.status IN ('PENDING', 'CONFIRMED') AND " +
           "b.startDate <= :endDate AND " +
           "b.endDate >= :startDate")
    List<Booking> findConflictingBookings(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find active bookings for a car
     */
    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId AND b.status IN ('PENDING', 'CONFIRMED')")
    List<Booking> findActiveBookingsForCar(@Param("carId") Long carId);

    /**
     * Find bookings starting today or in the future
     */
    @Query("SELECT b FROM Booking b WHERE b.startDate >= :date AND b.status = 'CONFIRMED'")
    List<Booking> findUpcomingBookings(@Param("date") LocalDate date);

    /**
     * Find bookings ending before a date (for cleanup/completion)
     */
    @Query("SELECT b FROM Booking b WHERE b.endDate < :date AND b.status = 'CONFIRMED'")
    List<Booking> findCompletableBookings(@Param("date") LocalDate date);

    /**
     * Find bookings within date range
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "(b.startDate BETWEEN :startDate AND :endDate) OR " +
           "(b.endDate BETWEEN :startDate AND :endDate)")
    List<Booking> findBookingsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Update booking status
     */
    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id = :bookingId")
    int updateBookingStatus(@Param("bookingId") Long bookingId, @Param("status") BookingStatus status);

    /**
     * Count bookings by status
     */
    long countByStatus(BookingStatus status);

    /**
     * Count bookings by user
     */
    long countByUserId(Long userId);

    /**
     * Get all bookings with pagination and sorting
     */
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Search bookings by reference or user email
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Booking> searchBookings(@Param("searchTerm") String searchTerm, Pageable pageable);
}
