package com.car_rental_backend.config;

import com.car_rental_backend.entity.Role;
import com.car_rental_backend.entity.User;
import com.car_rental_backend.enums.AccountStatus;
import com.car_rental_backend.enums.RoleType;
import com.car_rental_backend.repository.RoleRepository;
import com.car_rental_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data Initializer.
 * Creates default roles and admin user on application startup.
 * Only creates data if it doesn't already exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing application data...");
        
        initializeRoles();
        initializeAdminUser();
        
        log.info("Data initialization completed.");
    }

    /**
     * Initialize default roles if they don't exist
     */
    private void initializeRoles() {
        // Create ADMIN role
        if (!roleRepository.existsByName(RoleType.ROLE_ADMIN)) {
            Role adminRole = Role.builder()
                    .name(RoleType.ROLE_ADMIN)
                    .description("Administrator - Full system access")
                    .build();
            roleRepository.save(adminRole);
            log.info("Created ROLE_ADMIN");
        }

        // Create CUSTOMER role
        if (!roleRepository.existsByName(RoleType.ROLE_CUSTOMER)) {
            Role customerRole = Role.builder()
                    .name(RoleType.ROLE_CUSTOMER)
                    .description("Customer - Can book cars and view own bookings")
                    .build();
            roleRepository.save(customerRole);
            log.info("Created ROLE_CUSTOMER");
        }
    }

    /**
     * Initialize default admin user if no admin exists
     */
    private void initializeAdminUser() {
        String adminEmail = "admin@luxuryrentals.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User adminUser = User.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .phoneNumber("+1-555-0100")
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            adminUser.addRole(adminRole);
            userRepository.save(adminUser);

            log.info("=========================================");
            log.info("DEFAULT ADMIN ACCOUNT CREATED:");
            log.info("Email: {}", adminEmail);
            log.info("Password: Admin@123");
            log.info("IMPORTANT: Change this password immediately!");
            log.info("=========================================");
        }
    }
}
