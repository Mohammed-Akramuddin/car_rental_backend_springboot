package com.car_rental_backend.config;

import com.car_rental_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.client.RestTemplate;

/**
 * Application-wide configuration.
 * Provides beans for dependency injection.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * UserDetailsService implementation for Spring Security.
     * Loads user by email for authentication.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
