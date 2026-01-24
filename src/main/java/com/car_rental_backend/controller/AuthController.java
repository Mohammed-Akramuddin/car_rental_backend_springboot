package com.car_rental_backend.controller;

import com.car_rental_backend.dto.request.LoginRequest;
import com.car_rental_backend.dto.request.RegisterRequest;
import com.car_rental_backend.dto.response.ApiResponse;
import com.car_rental_backend.dto.response.AuthResponse;
import com.car_rental_backend.dto.response.RegisterResponse;
import com.car_rental_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller.
 * Handles user registration and login endpoints.
 * All endpoints are public (no authentication required).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new customer account. Sends activation email; no JWT until activated.
     *
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * Login with email and password
     * 
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Register a new admin account.
     * Requires X-Admin-Secret header to match ADMIN_REGISTRATION_SECRET env var.
     * If ADMIN_REGISTRATION_SECRET is not set, admin registration is disabled (403).
     *
     * POST /api/v1/auth/register/admin
     */
    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAdmin(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Admin-Secret", required = false) String adminSecret) {

        String expectedSecret = System.getenv("ADMIN_REGISTRATION_SECRET");
        if (expectedSecret == null || expectedSecret.isBlank()) {
            log.warn("Admin registration attempted but ADMIN_REGISTRATION_SECRET is not set");
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin registration is disabled"));
        }
        if (!expectedSecret.equals(adminSecret)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Invalid admin secret"));
        }

        log.info("Admin registration request received for email: {}", request.getEmail());
        AuthResponse response = authService.registerAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin registration successful", response));
    }

    /**
     * Activate account via email link. Returns HTML page.
     *
     * GET /api/v1/auth/activate?token=...
     */
    @GetMapping(value = "/activate", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> activate(@RequestParam(value = "token", required = false) String token) {
        String email = authService.activateAccount(token);
        String html = email != null
                ? buildActivationSuccessHtml()
                : buildActivationFailedHtml();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(html, headers, HttpStatus.OK);
    }

    /**
     * Health check endpoint
     *
     * GET /api/v1/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Auth service is running"));
    }

    private static String buildActivationSuccessHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Account activated</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; display: flex; align-items: center; justify-content: center; min-height: 100vh;">
                <div style="text-align: center; padding: 48px; max-width: 480px;">
                    <div style="background: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.08); padding: 40px;">
                        <h1 style="margin: 0 0 16px 0; font-size: 24px; color: #059669;">Your account has been activated</h1>
                        <p style="margin: 0; font-size: 16px; color: #4b5563; line-height: 1.5;">You can now log in and use the Car Rental service.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    private static String buildActivationFailedHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Activation failed</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; display: flex; align-items: center; justify-content: center; min-height: 100vh;">
                <div style="text-align: center; padding: 48px; max-width: 480px;">
                    <div style="background: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.08); padding: 40px;">
                        <h1 style="margin: 0 0 16px 0; font-size: 24px; color: #dc2626;">Activation link invalid or expired</h1>
                        <p style="margin: 0; font-size: 16px; color: #4b5563; line-height: 1.5;">Please register again or contact support.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
