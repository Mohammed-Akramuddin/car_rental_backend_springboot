# 🚗 Luxury Car Rental System - Backend API

A production-ready RESTful backend service for a luxury car rental platform built with **Spring Boot 3.2**, **Spring Security**, **JWT Authentication**, and **PostgreSQL**.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Sample API Requests](#-sample-api-requests)

---

## ✨ Features

### Authentication & Authorization
- ✅ User registration and login
- ✅ JWT-based stateless authentication
- ✅ Role-based access control (ADMIN, CUSTOMER)
- ✅ BCrypt password hashing
- ✅ Token validation and refresh

### User Management
- ✅ User profile management
- ✅ Account status control (Active, Disabled, Suspended)
- ✅ Admin user management

### Car Management
- ✅ Full CRUD operations for cars
- ✅ Category filtering (SUV, SEDAN, LUXURY, SPORTS, CONVERTIBLE)
- ✅ Status management (Available, Booked, Maintenance)
- ✅ Search and pagination
- ✅ Date-range availability checking

### Booking Management
- ✅ Create, view, cancel bookings
- ✅ **Double-booking prevention**
- ✅ Date range validation
- ✅ Automatic price calculation
- ✅ Booking lifecycle (Pending → Confirmed → Completed)
- ✅ Booking reference generation

---

## 🛠 Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 3.2.5 | Application Framework |
| Spring Security | 6.x | Security Framework |
| Spring Data JPA | 3.x | Data Persistence |
| PostgreSQL | 15+ | Database (Neon Cloud) |
| JWT (jjwt) | 0.12.5 | Token Authentication |
| Lombok | 1.18.x | Boilerplate Reduction |
| Maven | 3.9+ | Build Tool |

---

## 📁 Project Structure

```
car-rental-backend/
├── src/main/java/com/car_rental_backend/
│   ├── CarRentalApplication.java       # Main entry point
│   ├── config/
│   │   ├── ApplicationConfig.java      # App configuration
│   │   ├── SecurityConfig.java         # Security configuration
│   │   ├── JwtService.java             # JWT token service
│   │   └── JwtAuthenticationFilter.java # JWT filter
│   ├── controller/
│   │   ├── AuthController.java         # Authentication endpoints
│   │   ├── UserController.java         # User management
│   │   ├── CarController.java          # Car CRUD operations
│   │   └── BookingController.java      # Booking operations
│   ├── dto/
│   │   ├── request/                    # Request DTOs
│   │   └── response/                   # Response DTOs
│   ├── entity/
│   │   ├── User.java                   # User entity
│   │   ├── Role.java                   # Role entity
│   │   ├── Car.java                    # Car entity
│   │   └── Booking.java                # Booking entity
│   ├── enums/
│   │   ├── RoleType.java
│   │   ├── AccountStatus.java
│   │   ├── CarCategory.java
│   │   ├── CarStatus.java
│   │   └── BookingStatus.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   ├── CarRepository.java
│   │   └── BookingRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── CarService.java
│   │   └── BookingService.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ResourceNotFoundException.java
│       ├── BadRequestException.java
│       ├── UnauthorizedException.java
│       └── ConflictException.java
├── src/main/resources/
│   ├── application.yml                 # Application config
│   └── schema.sql                      # Database schema
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL database (or Neon cloud account)

### 1. Clone the Repository

```bash
git clone https://github.com/your-repo/car-rental-backend.git
cd car-rental-backend
```

### 2. Set Environment Variables

```bash
# Database (Neon PostgreSQL)
export DB_URL="jdbc:postgresql://your-neon-host.neon.tech:5432/car_rental?sslmode=require"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"

# JWT Secret (generate a secure 256-bit key)
export JWT_SECRET="your-256-bit-secret-key-here-at-least-32-chars"

# Admin Registration Secret (optional)
export ADMIN_REGISTRATION_SECRET="your-admin-secret"

# CORS Origins (comma-separated)
export CORS_ORIGINS="http://localhost:3000,http://localhost:5173"
```

### 3. Initialize Database

Run the schema SQL against your PostgreSQL database:

```bash
psql -h your-host -U your-username -d car_rental -f src/main/resources/schema.sql
```

### 4. Build and Run

```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Or run the JAR
java -jar target/car-rental-backend-1.0.0.jar
```

The API will be available at `http://localhost:8080`

---

## ⚙️ Configuration

### application.yml

Key configuration options:

```yaml
# Database
spring.datasource.url: ${DB_URL}
spring.datasource.username: ${DB_USERNAME}
spring.datasource.password: ${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto: validate  # Use 'update' for dev

# JWT
application.security.jwt.secret-key: ${JWT_SECRET}
application.security.jwt.expiration: 86400000  # 24 hours

# CORS
cors.allowed-origins: ${CORS_ORIGINS}
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints Overview

#### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new customer |
| POST | `/auth/login` | Login and get JWT |
| POST | `/auth/register/admin` | Register admin (protected) |

#### Users (Authenticated)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/users/me` | Get current user profile | Any |
| PUT | `/users/me` | Update current user profile | Any |
| GET | `/users/admin/all` | Get all users | Admin |
| GET | `/users/admin/{id}` | Get user by ID | Admin |
| PUT | `/users/admin/{id}` | Update user | Admin |
| PATCH | `/users/admin/{id}/status` | Update user status | Admin |
| DELETE | `/users/admin/{id}` | Disable user | Admin |

#### Cars (Public GET, Admin Write)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/cars` | Get all cars (paginated) | Public |
| GET | `/cars/{id}` | Get car by ID | Public |
| GET | `/cars/available` | Get available cars | Public |
| GET | `/cars/category/{category}` | Get cars by category | Public |
| GET | `/cars/search?query=` | Search cars | Public |
| GET | `/cars/available/dates` | Get available cars for dates | Public |
| GET | `/cars/{id}/availability` | Check car availability | Public |
| POST | `/cars` | Create new car | Admin |
| PUT | `/cars/{id}` | Update car | Admin |
| PATCH | `/cars/{id}/status` | Update car status | Admin |
| DELETE | `/cars/{id}` | Delete car | Admin |

#### Bookings (Authenticated)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/bookings` | Create booking | Customer |
| GET | `/bookings/my` | Get my bookings | Customer |
| GET | `/bookings/{id}` | Get booking by ID | Owner/Admin |
| GET | `/bookings/reference/{ref}` | Get by reference | Owner/Admin |
| POST | `/bookings/{id}/cancel` | Cancel booking | Owner/Admin |
| GET | `/bookings/admin/all` | Get all bookings | Admin |
| GET | `/bookings/admin/status/{status}` | Get by status | Admin |
| POST | `/bookings/admin/{id}/confirm` | Confirm booking | Admin |
| POST | `/bookings/admin/{id}/complete` | Complete booking | Admin |

---

## 🗄 Database Schema

### Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    USERS    │──────<│ USER_ROLES  │>──────│    ROLES    │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id          │       │ user_id     │       │ id          │
│ first_name  │       │ role_id     │       │ name        │
│ last_name   │       └─────────────┘       │ description │
│ email       │                             └─────────────┘
│ password    │
│ phone_number│       ┌─────────────┐
│ address     │       │  BOOKINGS   │
│ account_stat│──────<├─────────────┤
│ created_at  │       │ id          │
│ updated_at  │       │ booking_ref │
└─────────────┘       │ user_id     │
                      │ car_id      │>──────┌─────────────┐
                      │ start_date  │       │    CARS     │
                      │ end_date    │       ├─────────────┤
                      │ price_per_day       │ id          │
                      │ total_days  │       │ brand       │
                      │ total_price │       │ model       │
                      │ status      │       │ year        │
                      │ created_at  │       │ license_plate
                      └─────────────┘       │ category    │
                                            │ price_per_day
                                            │ status      │
                                            │ created_at  │
                                            └─────────────┘
```

---

## 🔐 Security

### JWT Token Flow

1. User logs in with email/password
2. Server validates credentials
3. Server generates JWT token with user claims
4. Client stores token and sends in `Authorization` header
5. Server validates token on each request

### Authorization Header Format
```
Authorization: Bearer <your-jwt-token>
```

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@#$%^&+=!)

---

## 📝 Sample API Requests

### Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "phoneNumber": "+1-555-0123",
    "driversLicense": "DL123456789"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "roles": ["ROLE_CUSTOMER"]
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }'
```

### Get Available Cars
```bash
curl http://localhost:8080/api/v1/cars/available?page=0&size=10
```

**Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "brand": "Mercedes-Benz",
        "model": "S-Class S580",
        "year": 2024,
        "category": "LUXURY",
        "pricePerDay": 599.99,
        "status": "AVAILABLE",
        "fullName": "2024 Mercedes-Benz S-Class S580"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 15,
    "totalPages": 2
  }
}
```

### Create Booking
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "carId": 1,
    "startDate": "2024-02-01",
    "endDate": "2024-02-05",
    "pickupLocation": "LAX Airport",
    "dropoffLocation": "LAX Airport",
    "notes": "Please have the car ready by 10 AM"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": 1,
    "bookingReference": "BK3A7F9C2E",
    "userId": 1,
    "userEmail": "john.doe@example.com",
    "carId": 1,
    "carFullName": "2024 Mercedes-Benz S-Class S580",
    "startDate": "2024-02-01",
    "endDate": "2024-02-05",
    "pricePerDay": 599.99,
    "totalDays": 5,
    "totalPrice": 2999.95,
    "status": "PENDING"
  }
}
```

### Admin: Create Car
```bash
curl -X POST http://localhost:8080/api/v1/cars \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "brand": "Aston Martin",
    "model": "DB12",
    "year": 2024,
    "licensePlate": "ASTN-001",
    "color": "Quantum Silver",
    "category": "SPORTS",
    "pricePerDay": 899.99,
    "description": "The latest grand tourer from Aston Martin",
    "seats": 2,
    "transmission": "Automatic",
    "fuelType": "Petrol"
  }'
```

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

---

## 📦 Deployment

### Docker

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/car-rental-backend-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Build and Run
```bash
docker build -t car-rental-backend .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/car_rental \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=pass \
  -e JWT_SECRET=your-secret \
  car-rental-backend
```

---

## 📄 License

This project is licensed under the MIT License.

---

## 👥 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📞 Support

For support, email support@luxuryrentals.com or open an issue.
