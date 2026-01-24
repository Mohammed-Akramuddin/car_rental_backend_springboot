package com.car_rental_backend.dto.request;

import com.car_rental_backend.enums.CarCategory;
import com.car_rental_backend.enums.CarStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for car creation/update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarRequest {

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand cannot exceed 50 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model cannot exceed 50 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    @Max(value = 2100, message = "Year cannot exceed 2100")
    private Integer year;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate cannot exceed 20 characters")
    private String licensePlate;

    @Size(max = 30, message = "Color cannot exceed 30 characters")
    private String color;

    @NotNull(message = "Category is required")
    private CarCategory category;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.01", message = "Price per day must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal pricePerDay;

    private CarStatus status;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    @Min(value = 1, message = "Seats must be at least 1")
    @Max(value = 20, message = "Seats cannot exceed 20")
    private Integer seats;

    @Size(max = 20, message = "Transmission cannot exceed 20 characters")
    private String transmission;

    @Size(max = 20, message = "Fuel type cannot exceed 20 characters")
    private String fuelType;

    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileage;
}
