package com.car_rental_backend.dto.response;

import com.car_rental_backend.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for booking response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private Long id;
    private String bookingReference;
    
    // User info
    private Long userId;
    private String userEmail;
    private String userFullName;
    
    // Car info
    private Long carId;
    private String carBrand;
    private String carModel;
    private String carFullName;
    private String carImageUrl;
    
    // Booking details
    private LocalDate startDate;
    private LocalDate endDate;
    private String pickupLocation;
    private String dropoffLocation;
    private BigDecimal pricePerDay;
    private Integer totalDays;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String notes;
    
    // Cancellation info
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
