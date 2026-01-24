package com.car_rental_backend.service;

import com.car_rental_backend.config.JwtService;
import com.car_rental_backend.dto.request.LoginRequest;
import com.car_rental_backend.dto.request.RegisterRequest;
import com.car_rental_backend.dto.response.AuthResponse;
import com.car_rental_backend.dto.response.RegisterResponse;
import com.car_rental_backend.entity.Role;
import com.car_rental_backend.entity.User;
import com.car_rental_backend.enums.AccountStatus;
import com.car_rental_backend.enums.RoleType;
import com.car_rental_backend.exception.BadRequestException;
import com.car_rental_backend.exception.UnauthorizedException;
import com.car_rental_backend.repository.RoleRepository;
import com.car_rental_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service.
 * Handles user registration, login, and token management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * Register a new customer user. Sends activation email; user must activate before login.
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Registering new user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered: " + email);
        }

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseGet(() -> {
                    Role newRole = new Role(RoleType.ROLE_CUSTOMER);
                    newRole.setDescription("Customer role - can book cars");
                    return roleRepository.save(newRole);
                });

        String activationToken = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime tokenExpiresAt = LocalDateTime.now().plusHours(EmailService.getActivationTokenValidHours());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .driversLicense(request.getDriversLicense())
                .accountStatus(AccountStatus.PENDING_ACTIVATION)
                .activationToken(activationToken)
                .activationTokenExpiresAt(tokenExpiresAt)
                .build();

        user.addRole(customerRole);
        User savedUser = userRepository.save(user);
        log.info("User registered successfully (pending activation): {}", savedUser.getEmail());

        emailService.sendActivationEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                activationToken
        );

        return RegisterResponse.builder()
                .email(savedUser.getEmail())
                .message("Registration successful. Please check your email to activate your account.")
                .build();
    }

    /**
     * Register a new admin user (should be called internally or by existing admin)
     */
    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Registering new admin with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered: " + email);
        }

        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role(RoleType.ROLE_ADMIN);
                    newRole.setDescription("Admin role - full system access");
                    return roleRepository.save(newRole);
                });

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        user.addRole(adminRole);

        User savedUser = userRepository.save(user);
        log.info("Admin registered successfully: {}", savedUser.getEmail());

        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(token, savedUser);
    }

    /**
     * Authenticate user and return JWT token. Rejects unactivated accounts with a clear message.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Login attempt for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", email);
                    return new UnauthorizedException("Invalid email or password");
                });

        if (user.getAccountStatus() == AccountStatus.PENDING_ACTIVATION) {
            log.warn("Login failed - account not activated: {}", email);
            throw new UnauthorizedException("Your account has not been activated yet. Please check your email and click the activation link.");
        }
        if (user.getAccountStatus() == AccountStatus.DISABLED) {
            log.warn("Login failed - account disabled: {}", email);
            throw new UnauthorizedException("Account is disabled. Please contact support.");
        }
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            log.warn("Login failed - account suspended: {}", email);
            throw new UnauthorizedException("Account is suspended. Please contact support.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials: {}", email);
            throw new UnauthorizedException("Invalid email or password");
        } catch (DisabledException e) {
            throw new UnauthorizedException("Account is disabled. Please contact support.");
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in successfully: {}", user.getEmail());
        return buildAuthResponse(token, user);
    }

    /**
     * Activate user account via token from email link. Clears token and sets status to ACTIVE.
     *
     * @return the activated user's email, or null if token invalid/expired
     */
    @Transactional
    public String activateAccount(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        User user = userRepository.findByActivationToken(token)
                .orElse(null);
        if (user == null) {
            return null;
        }
        if (user.getActivationTokenExpiresAt() == null
                || user.getActivationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Activation token expired for user: {}", user.getEmail());
            return null;
        }
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setActivationToken(null);
        user.setActivationTokenExpiresAt(null);
        userRepository.save(user);
        log.info("Account activated: {}", user.getEmail());
        return user.getEmail();
    }

    /**
     * Build authentication response
     */
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(AuthResponse.UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }
}
