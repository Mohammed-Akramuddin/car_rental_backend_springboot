package com.car_rental_backend.controller;

import com.car_rental_backend.dto.request.CarRequest;
import com.car_rental_backend.dto.response.ApiResponse;
import com.car_rental_backend.dto.response.CarDTO;
import com.car_rental_backend.dto.response.PagedResponse;
import com.car_rental_backend.enums.CarCategory;
import com.car_rental_backend.enums.CarStatus;
import com.car_rental_backend.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Car Controller.
 * Handles car inventory management endpoints.
 * 
 * Public: GET endpoints for viewing cars
 * Admin only: POST, PUT, PATCH, DELETE endpoints
 */
@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
@Slf4j
public class CarController {

    private final CarService carService;

    // ============================================
    // Public Endpoints (No authentication required)
    // ============================================

    /**
     * Get all cars with pagination
     * 
     * GET /api/v1/cars
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CarDTO>>> getAllCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PagedResponse<CarDTO> cars = carService.getAllCars(pageable);
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    /**
     * Get available cars
     * 
     * GET /api/v1/cars/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<PagedResponse<CarDTO>>> getAvailableCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "pricePerDay") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PagedResponse<CarDTO> cars = carService.getAvailableCars(pageable);
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    /**
     * Get car by ID
     * 
     * GET /api/v1/cars/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CarDTO>> getCarById(@PathVariable Long id) {
        CarDTO car = carService.getCarById(id);
        return ResponseEntity.ok(ApiResponse.success(car));
    }

    /**
     * Get cars by category
     * 
     * GET /api/v1/cars/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PagedResponse<CarDTO>>> getCarsByCategory(
            @PathVariable CarCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CarDTO> cars = carService.getCarsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    /**
     * Get cars by status
     * 
     * GET /api/v1/cars/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<CarDTO>>> getCarsByStatus(
            @PathVariable CarStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CarDTO> cars = carService.getCarsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    /**
     * Search cars by brand or model
     * 
     * GET /api/v1/cars/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<CarDTO>>> searchCars(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CarDTO> cars = carService.searchCars(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    /**
     * Get cars available for a date range
     * 
     * GET /api/v1/cars/available/dates
     */
    @GetMapping("/available/dates")
    public ResponseEntity<ApiResponse<List<CarDTO>>> getAvailableCarsForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) CarCategory category) {
        
        List<CarDTO> cars;
        if (category != null) {
            cars = carService.getAvailableCarsForDateRangeByCategory(startDate, endDate, category);
        } else {
            cars = carService.getAvailableCarsForDateRange(startDate, endDate);
        }
        
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    /**
     * Check if car is available for date range
     * 
     * GET /api/v1/cars/{id}/availability
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkCarAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        boolean available = carService.isCarAvailableForDateRange(id, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(
                available ? "Car is available" : "Car is not available", available));
    }

    /**
     * Get distinct brands for filtering
     * 
     * GET /api/v1/cars/brands
     */
    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<String>>> getDistinctBrands() {
        List<String> brands = carService.getDistinctBrands();
        return ResponseEntity.ok(ApiResponse.success(brands));
    }

    // ============================================
    // Admin Endpoints (Requires ADMIN role)
    // ============================================

    /**
     * Create a new car (Admin only)
     * 
     * POST /api/v1/cars
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarDTO>> createCar(@Valid @RequestBody CarRequest request) {
        log.info("Creating new car: {} {}", request.getBrand(), request.getModel());
        
        CarDTO car = carService.createCar(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Car created successfully", car));
    }

    /**
     * Update car (Admin only)
     * 
     * PUT /api/v1/cars/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarDTO>> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody CarRequest request) {
        log.info("Updating car ID: {}", id);
        
        CarDTO car = carService.updateCar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Car updated successfully", car));
    }

    /**
     * Update car status (Admin only)
     * 
     * PATCH /api/v1/cars/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarDTO>> updateCarStatus(
            @PathVariable Long id,
            @RequestParam CarStatus status) {
        log.info("Updating car ID: {} status to: {}", id, status);
        
        CarDTO car = carService.updateCarStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Car status updated to " + status, car));
    }

    /**
     * Delete car (Admin only)
     * 
     * DELETE /api/v1/cars/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCar(@PathVariable Long id) {
        log.info("Deleting car ID: {}", id);
        
        carService.deleteCar(id);
        return ResponseEntity.ok(ApiResponse.success("Car deleted successfully"));
    }
}
