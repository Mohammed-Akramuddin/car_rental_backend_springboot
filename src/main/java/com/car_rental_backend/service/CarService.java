package com.car_rental_backend.service;

import com.car_rental_backend.dto.request.CarRequest;
import com.car_rental_backend.dto.response.CarDTO;
import com.car_rental_backend.dto.response.PagedResponse;
import com.car_rental_backend.entity.Car;
import com.car_rental_backend.enums.CarCategory;
import com.car_rental_backend.enums.CarStatus;
import com.car_rental_backend.exception.BadRequestException;
import com.car_rental_backend.exception.ResourceNotFoundException;
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
import java.util.stream.Collectors;

/**
 * Car Service.
 * Handles car inventory management including CRUD operations
 * and availability checking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    /**
     * Create a new car (Admin only)
     */
    @Transactional
    public CarDTO createCar(CarRequest request) {
        log.info("Creating new car: {} {}", request.getBrand(), request.getModel());
        
        // Check for duplicate license plate
        if (carRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new BadRequestException("License plate already exists: " + request.getLicensePlate());
        }

        Car car = Car.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .licensePlate(request.getLicensePlate().toUpperCase())
                .color(request.getColor())
                .category(request.getCategory())
                .pricePerDay(request.getPricePerDay())
                .status(request.getStatus() != null ? request.getStatus() : CarStatus.AVAILABLE)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .seats(request.getSeats() != null ? request.getSeats() : 4)
                .transmission(request.getTransmission() != null ? request.getTransmission() : "Automatic")
                .fuelType(request.getFuelType() != null ? request.getFuelType() : "Petrol")
                .mileage(request.getMileage())
                .build();

        Car savedCar = carRepository.save(car);
        log.info("Car created successfully with ID: {}", savedCar.getId());

        return mapToDTO(savedCar);
    }

    /**
     * Get car by ID
     */
    @Transactional(readOnly = true)
    public CarDTO getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + id));
        return mapToDTO(car);
    }

    /**
     * Get all cars with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<CarDTO> getAllCars(Pageable pageable) {
        Page<Car> cars = carRepository.findAll(pageable);
        return PagedResponse.from(cars, 
                cars.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Get available cars with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<CarDTO> getAvailableCars(Pageable pageable) {
        Page<Car> cars = carRepository.findAllAvailable(pageable);
        return PagedResponse.from(cars, 
                cars.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Get cars by category
     */
    @Transactional(readOnly = true)
    public PagedResponse<CarDTO> getCarsByCategory(CarCategory category, Pageable pageable) {
        Page<Car> cars = carRepository.findByCategory(category, pageable);
        return PagedResponse.from(cars, 
                cars.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Get cars by status
     */
    @Transactional(readOnly = true)
    public PagedResponse<CarDTO> getCarsByStatus(CarStatus status, Pageable pageable) {
        Page<Car> cars = carRepository.findByStatus(status, pageable);
        return PagedResponse.from(cars, 
                cars.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Search cars
     */
    @Transactional(readOnly = true)
    public PagedResponse<CarDTO> searchCars(String searchTerm, Pageable pageable) {
        Page<Car> cars = carRepository.searchCars(searchTerm, pageable);
        return PagedResponse.from(cars, 
                cars.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Get cars available for a date range
     */
    @Transactional(readOnly = true)
    public List<CarDTO> getAvailableCarsForDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        List<Car> cars = carRepository.findAvailableCarsForDateRange(startDate, endDate);
        return cars.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get cars available for date range by category
     */
    @Transactional(readOnly = true)
    public List<CarDTO> getAvailableCarsForDateRangeByCategory(
            LocalDate startDate, LocalDate endDate, CarCategory category) {
        validateDateRange(startDate, endDate);
        
        List<Car> cars = carRepository.findAvailableCarsForDateRangeByCategory(
                startDate, endDate, category);
        return cars.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if car is available for date range
     */
    @Transactional(readOnly = true)
    public boolean isCarAvailableForDateRange(Long carId, LocalDate startDate, LocalDate endDate) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + carId));
        
        if (car.getStatus() != CarStatus.AVAILABLE) {
            return false;
        }
        
        return !bookingRepository.existsConflictingBooking(carId, startDate, endDate);
    }

    /**
     * Update car (Admin only)
     */
    @Transactional
    public CarDTO updateCar(Long id, CarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + id));

        // Check license plate uniqueness if changed
        if (!car.getLicensePlate().equalsIgnoreCase(request.getLicensePlate())) {
            if (carRepository.existsByLicensePlate(request.getLicensePlate())) {
                throw new BadRequestException("License plate already exists: " + request.getLicensePlate());
            }
        }

        // Update fields
        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setLicensePlate(request.getLicensePlate().toUpperCase());
        car.setColor(request.getColor());
        car.setCategory(request.getCategory());
        car.setPricePerDay(request.getPricePerDay());
        
        if (request.getStatus() != null) {
            // Check if car has active bookings before changing to MAINTENANCE
            if (request.getStatus() == CarStatus.MAINTENANCE) {
                if (bookingRepository.findActiveBookingsForCar(id).size() > 0) {
                    throw new BadRequestException(
                        "Cannot set car to maintenance - has active bookings"
                    );
                }
            }
            car.setStatus(request.getStatus());
        }
        
        car.setDescription(request.getDescription());
        car.setImageUrl(request.getImageUrl());
        if (request.getSeats() != null) car.setSeats(request.getSeats());
        if (request.getTransmission() != null) car.setTransmission(request.getTransmission());
        if (request.getFuelType() != null) car.setFuelType(request.getFuelType());
        if (request.getMileage() != null) car.setMileage(request.getMileage());

        Car updatedCar = carRepository.save(car);
        log.info("Car {} updated successfully", id);

        return mapToDTO(updatedCar);
    }

    /**
     * Update car status (Admin only)
     */
    @Transactional
    public CarDTO updateCarStatus(Long id, CarStatus status) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + id));

        if (status == CarStatus.MAINTENANCE) {
            if (bookingRepository.findActiveBookingsForCar(id).size() > 0) {
                throw new BadRequestException(
                    "Cannot set car to maintenance - has active bookings"
                );
            }
        }

        car.setStatus(status);
        Car updatedCar = carRepository.save(car);
        log.info("Car {} status updated to {}", id, status);

        return mapToDTO(updatedCar);
    }

    /**
     * Delete car (Admin only)
     */
    @Transactional
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + id));

        // Check for active bookings
        if (bookingRepository.findActiveBookingsForCar(id).size() > 0) {
            throw new BadRequestException("Cannot delete car - has active bookings");
        }

        carRepository.delete(car);
        log.info("Car {} deleted successfully", id);
    }

    /**
     * Get distinct brands for filtering
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctBrands() {
        return carRepository.findDistinctBrands();
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
     * Map Car entity to CarDTO
     */
    private CarDTO mapToDTO(Car car) {
        return CarDTO.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .year(car.getYear())
                .licensePlate(car.getLicensePlate())
                .color(car.getColor())
                .category(car.getCategory())
                .pricePerDay(car.getPricePerDay())
                .status(car.getStatus())
                .description(car.getDescription())
                .imageUrl(car.getImageUrl())
                .seats(car.getSeats())
                .transmission(car.getTransmission())
                .fuelType(car.getFuelType())
                .mileage(car.getMileage())
                .fullName(car.getFullName())
                .createdAt(car.getCreatedAt())
                .updatedAt(car.getUpdatedAt())
                .build();
    }
}
