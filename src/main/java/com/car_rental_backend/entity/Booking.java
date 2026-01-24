package com.car_rental_backend.entity;

import com.car_rental_backend.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Booking entity representing car rental reservations.
 * 
 * Features:
 * - Links users to cars for specific date ranges
 * - Automatic total price calculation
 * - Status lifecycle management (PENDING -> CONFIRMED -> COMPLETED)
 * - Prevents double booking via business logic in service layer
 */
@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_user", columnList = "user_id"),
    @Index(name = "idx_booking_car", columnList = "car_id"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_booking_car_dates", columnList = "car_id, start_date, end_date")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 20)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "pickup_location", length = 255)
    private String pickupLocation;

    @Column(name = "dropoff_location", length = 255)
    private String dropoffLocation;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================
    // Business Logic Methods
    // ============================================

    /**
     * Calculates the number of rental days between start and end date.
     * Minimum is 1 day.
     */
    public int calculateTotalDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // Inclusive
        return Math.max((int) days, 1);
    }

    /**
     * Calculates total price based on days and price per day.
     */
    public BigDecimal calculateTotalPrice() {
        if (pricePerDay == null || totalDays == null || totalDays <= 0) {
            return BigDecimal.ZERO;
        }
        return pricePerDay.multiply(BigDecimal.valueOf(totalDays));
    }

    /**
     * Initializes calculated fields before persisting.
     */
    @PrePersist
    @PreUpdate
    public void calculateFields() {
        this.totalDays = calculateTotalDays();
        this.totalPrice = calculateTotalPrice();
    }

    /**
     * Confirms the booking
     */
    public void confirm() {
        if (this.status == BookingStatus.PENDING) {
            this.status = BookingStatus.CONFIRMED;
        }
    }

    /**
     * Cancels the booking with a reason
     */
    public void cancel(String reason) {
        if (this.status != BookingStatus.COMPLETED) {
            this.status = BookingStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
            this.cancellationReason = reason;
        }
    }

    /**
     * Completes the booking
     */
    public void complete() {
        if (this.status == BookingStatus.CONFIRMED) {
            this.status = BookingStatus.COMPLETED;
        }
    }

    /**
     * Checks if booking dates overlap with given date range
     */
    public boolean overlapsWithDateRange(LocalDate otherStart, LocalDate otherEnd) {
        return !startDate.isAfter(otherEnd) && !endDate.isBefore(otherStart);
    }

    /**
     * Checks if booking is active (PENDING or CONFIRMED)
     */
    public boolean isActive() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    /**
     * Checks if booking can be cancelled
     */
    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking booking)) return false;
        return Objects.equals(id, booking.id) && 
               Objects.equals(bookingReference, booking.bookingReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bookingReference);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingReference='" + bookingReference + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                '}';
    }
}
