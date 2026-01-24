package com.car_rental_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Luxury Car Rental System.
 * 
 * This application provides:
 * - User authentication & authorization (JWT-based)
 * - Car inventory management
 * - Booking management with conflict prevention
 * - Role-based access control (ADMIN, CUSTOMER)
 * 
 * @author Car Rental Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing  // Enables automatic timestamp population
public class CarRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarRentalApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════════╗
            ║                                                              ║
            ║     🚗  LUXURY CAR RENTAL SYSTEM - STARTED SUCCESSFULLY  🚗  ║
            ║                                                              ║
            ║     API Documentation: http://localhost:8080/api             ║
            ║     Health Check: http://localhost:8080/actuator/health      ║
            ║                                                              ║
            ╚══════════════════════════════════════════════════════════════╝
            """);
    }
}
