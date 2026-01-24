package com.car_rental_backend.dto.response;

import com.car_rental_backend.enums.CarCategory;
import com.car_rental_backend.enums.CarStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for car response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarDTO {

    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private String color;
    private CarCategory category;
    private BigDecimal pricePerDay;
    private CarStatus status;
    private String description;
    private String imageUrl;
    private Integer seats;
    private String transmission;
    private String fuelType;
    private Integer mileage;
    private String fullName;  // Computed: Year Brand Model
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
