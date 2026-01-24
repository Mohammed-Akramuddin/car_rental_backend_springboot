package com.car_rental_backend.repository;

import com.car_rental_backend.entity.User;
import com.car_rental_backend.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (used for authentication)
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by activation token
     */
    Optional<User> findByActivationToken(String activationToken);

    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by account status
     */
    List<User> findByAccountStatus(AccountStatus status);

    /**
     * Find all users with pagination
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Find users by account status with pagination
     */
    Page<User> findByAccountStatus(AccountStatus status, Pageable pageable);

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Update user account status
     */
    @Modifying
    @Query("UPDATE User u SET u.accountStatus = :status WHERE u.id = :userId")
    int updateAccountStatus(@Param("userId") Long userId, @Param("status") AccountStatus status);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRole(@Param("roleName") String roleName);

    /**
     * Find users with specific role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
