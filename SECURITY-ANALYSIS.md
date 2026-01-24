# Security Analysis – Car Rental Backend

This document summarizes the security configuration review and fixes applied.

---

## What Is Correctly Configured

### 1. **Authentication & JWT**
- Stateless JWT auth with `OncePerRequestFilter`; no session cookies.
- Token extracted from `Authorization: Bearer <token>`.
- `JwtService` validates signature, expiry, and structure; uses HS256.
- Token checked against `UserDetails` (username match, not expired).
- BCrypt password encoder (strength 12) for storage.

### 2. **Authorization**
- `@EnableMethodSecurity(prePostEnabled = true)` and `@PreAuthorize` on admin endpoints.
- Role-based rules:
  - **Public:** `/api/v1/auth/**`, GET `/api/v1/cars/**`, `/actuator/health`, `/actuator/info`, `/`, `/error`, Swagger paths.
  - **Authenticated:** `/api/v1/users/me`, `/api/v1/bookings/**` (non-admin).
  - **Admin only:** POST/PUT/PATCH/DELETE `/api/v1/cars/**`, `/api/v1/users/admin/**`, `/api/v1/bookings/admin/**`.
- `User.getAuthorities()` returns `ROLE_ADMIN` / `ROLE_CUSTOMER`; `hasRole("ADMIN")` works correctly.

### 3. **Resource-Level Checks**
- `BookingService.getBookingById` / `getBookingByReference`: users can only access their own bookings unless admin.
- Ownership enforced before returning data.

### 4. **Account Status**
- `User.isEnabled()` → `ACTIVE` only; `isAccountNonLocked()` → not `SUSPENDED`.
- Login rejects disabled/suspended via `DisabledException` / `LockedException` and explicit status check.
- Generic “Invalid email or password” / “Account is disabled” – no user enumeration.

### 5. **CORS**
- Specific origins from config (`CORS_ORIGINS`), not `*`.
- `allowCredentials(true)`, methods and headers from config.
- `Authorization` exposed to frontend.

### 6. **CSRF**
- CSRF disabled; appropriate for stateless JWT API.

### 7. **Error Handling**
- `GlobalExceptionHandler` returns generic messages to clients (e.g. “An unexpected error occurred”).
- No stack traces or sensitive details in API responses.
- 401/403 JSON responses via `AuthenticationEntryPoint` and `AccessDeniedHandler`.

### 8. **Validation**
- `@Valid` on auth DTOs; password rules (length, complexity) on `RegisterRequest`.
- Validation errors returned in a structured way.

### 9. **JWT Filter**
- Skips OPTIONS (CORS preflight).
- Skips “public” paths (auth, actuator, swagger) to avoid unnecessary DB lookups; others still go through Security filter chain.

---

## Fix Applied

### Admin registration vulnerability (fixed)

**Issue:** `POST /api/v1/auth/register/admin` was public when `ADMIN_REGISTRATION_SECRET` was **not** set. The code only checked the secret when the env var was present, so anyone could create an admin account if the var was unset.

**Change:**
- If `ADMIN_REGISTRATION_SECRET` is **not** set or blank → **403** “Admin registration is disabled.”
- If set but `X-Admin-Secret` does not match → **403** “Invalid admin secret.”
- Admin registration only succeeds when the env var is set **and** the header matches.

**Usage:** Set `ADMIN_REGISTRATION_SECRET` in production. When creating an admin, send `X-Admin-Secret: <value>`.

---

## Recommendations

### 1. **JWT secret (production)**
- Set `JWT_SECRET` env var in production.
- Use a Base64-encoded 256-bit+ key. Example: `openssl rand -base64 32`.
- Do not rely on the default value in config.

### 2. **Admin registration**
- Set `ADMIN_REGISTRATION_SECRET` in production if you use `/register/admin`.
- Use a long, random value and keep it secret.
- Consider disabling or moving this flow to an internal admin/CLI process.

### 3. **Rate limiting**
- Add rate limiting (e.g. Bucket4j, Resilience4j, or reverse proxy) for:
  - `/api/v1/auth/login`
  - `/api/v1/auth/register`
- Mitigates brute-force and abuse.

### 4. **HTTPS**
- Use HTTPS in production; JWTs in `Authorization` header are still sensitive.

### 5. **Actuator**
- `health` and `info` are public; `metrics` is “when_authorized.”
- Ensure no sensitive data is exposed via actuators; restrict exposure if needed.

### 6. **Swagger / OpenAPI**
- Swagger paths are public in config.
- If you add Springdoc, consider protecting `/swagger-ui` and `/v3/api-docs` in production or disabling them.

---

## Summary

Core security (JWT, roles, CORS, CSRF, validation, error handling, booking ownership) is correctly set up. The **admin registration** vulnerability has been fixed. Apply the recommendations above (especially JWT secret, admin secret, and rate limiting) for production.
