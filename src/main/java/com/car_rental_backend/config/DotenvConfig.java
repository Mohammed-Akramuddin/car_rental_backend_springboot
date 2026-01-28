package com.car_rental_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class to load environment variables from .env file.
 * This runs before Spring Boot initializes, ensuring all environment variables
 * are available when application.yml is processed.
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Load .env file
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // Convert dotenv entries to a Map
            Map<String, Object> dotenvProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvProperties.put(entry.getKey(), entry.getValue());
                // Also set as system property for compatibility
                System.setProperty(entry.getKey(), entry.getValue());
            });

            // Add to Spring Environment
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenvProperties", dotenvProperties));

            System.out.println("✅ Successfully loaded .env file with " + dotenvProperties.size() + " properties");
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Could not load .env file - " + e.getMessage());
        }
    }
}
