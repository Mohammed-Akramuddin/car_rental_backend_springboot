# Car Rental Backend – API Endpoints Reference

**Base URL:** `http://localhost:8081`

All API endpoints use the `/api/v1` prefix unless noted.

---

## 1. Authentication (`/api/v1/auth`)

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| `POST` | `http://localhost:8081/api/v1/auth/register` | Public | Register new customer |
| `POST` | `http://localhost:8081/api/v1/auth/login` | Public | Login (returns JWT) |
| `POST` | `http://localhost:8081/api/v1/auth/register/admin` | Public | Register new admin |
| `GET` | `http://localhost:8081/api/v1/auth/health` | Public | Health check |

---

## 2. Cars (`/api/v1/cars`)

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| `GET` | `http://localhost:8081/api/v1/cars` | Public | List all cars (paged) |
| `GET` | `http://localhost:8081/api/v1/cars/available` | Public | List available cars (paged) |
| `GET` | `http://localhost:8081/api/v1/cars/{id}` | Public | Get car by ID |
| `GET` | `http://localhost:8081/api/v1/cars/category/{category}` | Public | Cars by category (SUV, SEDAN, LUXURY, SPORTS, CONVERTIBLE) |
| `GET` | `http://localhost:8081/api/v1/cars/status/{status}` | Public | Cars by status (AVAILABLE, BOOKED, MAINTENANCE) |
| `GET` | `http://localhost:8081/api/v1/cars/search` | Public | Search cars (query params) |
| `GET` | `http://localhost:8081/api/v1/cars/available/dates` | Public | Cars available for date range (query: `startDate`, `endDate`) |
| `GET` | `http://localhost:8081/api/v1/cars/{id}/availability` | Public | Check car availability for dates (query: `startDate`, `endDate`) |
| `GET` | `http://localhost:8081/api/v1/cars/brands` | Public | List distinct car brands |
| `POST` | `http://localhost:8081/api/v1/cars` | Admin | Create car |
| `PUT` | `http://localhost:8081/api/v1/cars/{id}` | Admin | Update car |
| `PATCH` | `http://localhost:8081/api/v1/cars/{id}/status` | Admin | Update car status |
| `DELETE` | `http://localhost:8081/api/v1/cars/{id}` | Admin | Delete car |

---

## 3. Bookings (`/api/v1/bookings`)

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| `POST` | `http://localhost:8081/api/v1/bookings` | Customer/Admin | Create booking |
| `GET` | `http://localhost:8081/api/v1/bookings/my` | Customer/Admin | Get current user's bookings |
| `GET` | `http://localhost:8081/api/v1/bookings/my/paged` | Customer/Admin | Get current user's bookings (paged) |
| `GET` | `http://localhost:8081/api/v1/bookings/{id}` | Customer/Admin | Get booking by ID |
| `GET` | `http://localhost:8081/api/v1/bookings/reference/{reference}` | Customer/Admin | Get booking by reference |
| `POST` | `http://localhost:8081/api/v1/bookings/{id}/cancel` | Customer/Admin | Cancel booking |
| `GET` | `http://localhost:8081/api/v1/bookings/admin/all` | Admin | List all bookings (paged) |
| `GET` | `http://localhost:8081/api/v1/bookings/admin/status/{status}` | Admin | Bookings by status |
| `GET` | `http://localhost:8081/api/v1/bookings/admin/search` | Admin | Search bookings |
| `GET` | `http://localhost:8081/api/v1/bookings/admin/car/{carId}` | Admin | Bookings for a car |
| `GET` | `http://localhost:8081/api/v1/bookings/admin/upcoming` | Admin | Upcoming bookings |
| `POST` | `http://localhost:8081/api/v1/bookings/admin/{id}/confirm` | Admin | Confirm booking |
| `POST` | `http://localhost:8081/api/v1/bookings/admin/{id}/complete` | Admin | Mark booking complete |

---

## 4. Users (`/api/v1/users`)

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| `GET` | `http://localhost:8081/api/v1/users/me` | Logged-in | Get current user profile |
| `PUT` | `http://localhost:8081/api/v1/users/me` | Logged-in | Update current user profile |
| `GET` | `http://localhost:8081/api/v1/users/admin/all` | Admin | List all users (paged) |
| `GET` | `http://localhost:8081/api/v1/users/admin/search` | Admin | Search users |
| `GET` | `http://localhost:8081/api/v1/users/admin/{id}` | Admin | Get user by ID |
| `PUT` | `http://localhost:8081/api/v1/users/admin/{id}` | Admin | Update user |
| `PATCH` | `http://localhost:8081/api/v1/users/admin/{id}/status` | Admin | Update user account status |
| `DELETE` | `http://localhost:8081/api/v1/users/admin/{id}` | Admin | Delete user |

---

## 5. Actuator (Spring Boot)

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| `GET` | `http://localhost:8081/actuator/health` | Public | Health check |
| `GET` | `http://localhost:8081/actuator/info` | Public | Application info |
| `GET` | `http://localhost:8081/actuator/metrics` | When authorized | Metrics |

---

## 6. Swagger / OpenAPI (if configured)

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| `GET` | `http://localhost:8081/swagger-ui/index.html` | Public | Swagger UI |
| `GET` | `http://localhost:8081/v3/api-docs` | Public | OpenAPI JSON |

*Note: Swagger/OpenAPI is referenced in SecurityConfig but no Springdoc dependency was found in `pom.xml`. These URLs may 404 unless you add `springdoc-openapi-starter-webmvc-ui`.*

---

## Authentication

- **Public:** No `Authorization` header.
- **Logged-in:** Send JWT in header: `Authorization: Bearer <token>`.
- **Admin:** Same as logged-in; user must have `ROLE_ADMIN`.

---

## Quick reference – all URLs (one per line)

```
http://localhost:8081/api/v1/auth/register
http://localhost:8081/api/v1/auth/login
http://localhost:8081/api/v1/auth/register/admin
http://localhost:8081/api/v1/auth/health
http://localhost:8081/api/v1/cars
http://localhost:8081/api/v1/cars/available
http://localhost:8081/api/v1/cars/{id}
http://localhost:8081/api/v1/cars/category/{category}
http://localhost:8081/api/v1/cars/status/{status}
http://localhost:8081/api/v1/cars/search
http://localhost:8081/api/v1/cars/available/dates
http://localhost:8081/api/v1/cars/{id}/availability
http://localhost:8081/api/v1/cars/brands
http://localhost:8081/api/v1/bookings
http://localhost:8081/api/v1/bookings/my
http://localhost:8081/api/v1/bookings/my/paged
http://localhost:8081/api/v1/bookings/{id}
http://localhost:8081/api/v1/bookings/reference/{reference}
http://localhost:8081/api/v1/bookings/{id}/cancel
http://localhost:8081/api/v1/bookings/admin/all
http://localhost:8081/api/v1/bookings/admin/status/{status}
http://localhost:8081/api/v1/bookings/admin/search
http://localhost:8081/api/v1/bookings/admin/car/{carId}
http://localhost:8081/api/v1/bookings/admin/upcoming
http://localhost:8081/api/v1/bookings/admin/{id}/confirm
http://localhost:8081/api/v1/bookings/admin/{id}/complete
http://localhost:8081/api/v1/users/me
http://localhost:8081/api/v1/users/admin/all
http://localhost:8081/api/v1/users/admin/search
http://localhost:8081/api/v1/users/admin/{id}
http://localhost:8081/actuator/health
http://localhost:8081/actuator/info
http://localhost:8081/actuator/metrics
```

Replace `{id}`, `{category}`, `{status}`, `{reference}`, `{carId}` with actual values when calling the APIs.
