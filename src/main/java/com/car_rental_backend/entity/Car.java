package com.car_rental_backend.entity;

import com.car_rental_backend.enums.CarCategory;
import com.car_rental_backend.enums.CarStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Car entity representing luxury vehicles available for rental.
 * 
 * Features:
 * - Categorization (SUV, SEDAN, LUXURY, SPORTS, CONVERTIBLE)
 * - Status tracking (AVAILABLE, BOOKED, MAINTENANCE)
 * - Price per day management
 * - Relationship with bookings
 */
@Entity
@Table(name = "cars", indexes = {
    @Index(name = "idx_car_status", columnList = "status"),
    @Index(name = "idx_car_category", columnList = "category"),
    @Index(name = "idx_car_brand", columnList = "brand"),
    @Index(name = "idx_car_available", columnList = "status, category")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "color", length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private CarCategory category;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CarStatus status = CarStatus.AVAILABLE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "seats")
    @Builder.Default
    private Integer seats = 4;

    @Column(name = "transmission", length = 20)
    @Builder.Default
    private String transmission = "Automatic";

    @Column(name = "fuel_type", length = 20)
    @Builder.Default
    private String fuelType = "Petrol";

    @Column(name = "mileage")
    private Integer mileage;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================
    // Helper Methods
    // ============================================

    /**
     * Returns the full car name (Brand + Model + Year)
     */
    public String getFullName() {
        return year + " " + brand + " " + model;
    }

    /**
     * Checks if the car is available for booking
     */
    public boolean isAvailable() {
        return status == CarStatus.AVAILABLE;
    }

    /**
     * Sets the car status to maintenance
     */
    public void setToMaintenance() {
        this.status = CarStatus.MAINTENANCE;
    }

    /**
     * Sets the car status to available
     */
    public void setToAvailable() {
        this.status = CarStatus.AVAILABLE;
    }

    /**
     * Adds a booking to this car
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setCar(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car car)) return false;
        return Objects.equals(id, car.id) && Objects.equals(licensePlate, car.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, licensePlate);
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", category=" + category +
                ", status=" + status +
                ", pricePerDay=" + pricePerDay +
                '}';
    }
}
