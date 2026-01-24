package com.car_rental_backend.repository;

import com.car_rental_backend.entity.Car;
import com.car_rental_backend.enums.CarCategory;
import com.car_rental_backend.enums.CarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Car entity.
 * Provides CRUD operations and advanced queries for car management.
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    /**
     * Find car by license plate
     */
    Optional<Car> findByLicensePlate(String licensePlate);

    /**
     * Check if license plate exists
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * Find cars by status
     */
    List<Car> findByStatus(CarStatus status);

    /**
     * Find cars by status with pagination
     */
    Page<Car> findByStatus(CarStatus status, Pageable pageable);

    /**
     * Find cars by category
     */
    List<Car> findByCategory(CarCategory category);

    /**
     * Find cars by category with pagination
     */
    Page<Car> findByCategory(CarCategory category, Pageable pageable);

    /**
     * Find available cars by category
     */
    List<Car> findByStatusAndCategory(CarStatus status, CarCategory category);

    /**
     * Find cars by brand
     */
    List<Car> findByBrandIgnoreCase(String brand);

    /**
     * Find cars within price range
     */
    @Query("SELECT c FROM Car c WHERE c.pricePerDay BETWEEN :minPrice AND :maxPrice")
    List<Car> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     * Search cars by brand or model
     */
    @Query("SELECT c FROM Car c WHERE " +
           "LOWER(c.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.model) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Car> searchCars(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find all available cars
     */
    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE'")
    List<Car> findAllAvailable();

    /**
     * Find available cars with pagination
     */
    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE'")
    Page<Car> findAllAvailable(Pageable pageable);

    /**
     * Find cars available for a specific date range (no conflicting bookings)
     */
    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE' AND c.id NOT IN (" +
           "SELECT b.car.id FROM Booking b WHERE " +
           "b.status IN ('PENDING', 'CONFIRMED') AND " +
           "b.startDate <= :endDate AND b.endDate >= :startDate)")
    List<Car> findAvailableCarsForDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find cars available for date range with category filter
     */
    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE' AND c.category = :category AND c.id NOT IN (" +
           "SELECT b.car.id FROM Booking b WHERE " +
           "b.status IN ('PENDING', 'CONFIRMED') AND " +
           "b.startDate <= :endDate AND b.endDate >= :startDate)")
    List<Car> findAvailableCarsForDateRangeByCategory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") CarCategory category
    );

    /**
     * Update car status
     */
    @Modifying
    @Query("UPDATE Car c SET c.status = :status WHERE c.id = :carId")
    int updateCarStatus(@Param("carId") Long carId, @Param("status") CarStatus status);

    /**
     * Count cars by status
     */
    long countByStatus(CarStatus status);

    /**
     * Count cars by category
     */
    long countByCategory(CarCategory category);

    /**
     * Get distinct brands
     */
    @Query("SELECT DISTINCT c.brand FROM Car c ORDER BY c.brand")
    List<String> findDistinctBrands();
}
