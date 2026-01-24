package com.car_rental_backend.repository;

import com.car_rental_backend.entity.Role;
import com.car_rental_backend.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 * Provides operations for role management.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(RoleType name);

    /**
     * Check if role exists
     */
    boolean existsByName(RoleType name);
}
