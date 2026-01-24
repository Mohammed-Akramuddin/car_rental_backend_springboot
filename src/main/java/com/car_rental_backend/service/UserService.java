package com.car_rental_backend.service;

import com.car_rental_backend.dto.request.UpdateUserRequest;
import com.car_rental_backend.dto.response.PagedResponse;
import com.car_rental_backend.dto.response.UserDTO;
import com.car_rental_backend.entity.User;
import com.car_rental_backend.enums.AccountStatus;
import com.car_rental_backend.exception.BadRequestException;
import com.car_rental_backend.exception.ResourceNotFoundException;
import com.car_rental_backend.repository.BookingRepository;
import com.car_rental_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * User Service.
 * Handles user profile management and admin user operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        User user = getAuthenticatedUser();
        return mapToDTO(user);
    }

    /**
     * Update current user's profile
     */
    @Transactional
    public UserDTO updateCurrentUser(UpdateUserRequest request) {
        User user = getAuthenticatedUser();
        
        // Update allowed fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getDriversLicense() != null) {
            user.setDriversLicense(request.getDriversLicense());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User profile updated: {}", updatedUser.getEmail());
        
        return mapToDTO(updatedUser);
    }

    /**
     * Get user by ID (Admin only)
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToDTO(user);
    }

    /**
     * Get all users with pagination (Admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return PagedResponse.from(users, 
                users.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Search users (Admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> searchUsers(String searchTerm, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return PagedResponse.from(users, 
                users.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    /**
     * Update user status (Admin only)
     */
    @Transactional
    public UserDTO updateUserStatus(Long userId, AccountStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Prevent admin from disabling themselves
        User currentUser = getAuthenticatedUser();
        if (user.getId().equals(currentUser.getId()) && status != AccountStatus.ACTIVE) {
            throw new BadRequestException("Cannot disable your own account");
        }
        
        user.setAccountStatus(status);
        User updatedUser = userRepository.save(user);
        
        log.info("User {} status updated to {} by admin {}", 
                user.getEmail(), status, currentUser.getEmail());
        
        return mapToDTO(updatedUser);
    }

    /**
     * Update user by ID (Admin only)
     */
    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getDriversLicense() != null) {
            user.setDriversLicense(request.getDriversLicense());
        }
        if (request.getAccountStatus() != null) {
            // Prevent admin from disabling themselves
            User currentUser = getAuthenticatedUser();
            if (user.getId().equals(currentUser.getId()) && 
                request.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new BadRequestException("Cannot disable your own account");
            }
            user.setAccountStatus(request.getAccountStatus());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User {} updated by admin", user.getEmail());
        
        return mapToDTO(updatedUser);
    }

    /**
     * Delete user (Admin only) - Soft delete by disabling
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        User currentUser = getAuthenticatedUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Cannot delete your own account");
        }
        
        // Soft delete - disable the account
        user.setAccountStatus(AccountStatus.DISABLED);
        userRepository.save(user);
        
        log.info("User {} disabled by admin {}", user.getEmail(), currentUser.getEmail());
    }

    /**
     * Get authenticated user from security context
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No authenticated user found");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * Map User entity to UserDTO
     */
    private UserDTO mapToDTO(User user) {
        int bookingCount = (int) bookingRepository.countByUserId(user.getId());
        
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .driversLicense(user.getDriversLicense())
                .accountStatus(user.getAccountStatus())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalBookings(bookingCount)
                .build();
    }
}
