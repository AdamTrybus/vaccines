package org.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://frontend:3000");
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("http://gateway:8080");
        configuration.addAllowedOrigin("http://localhost:8082");
        configuration.addAllowedOrigin("http://producer:8082");

        // Allow any HTTP methods (GET, POST, etc.)
        configuration.addAllowedMethod("*");

        // Allow any headers
        configuration.addAllowedHeader("*");

        // Allow credentials (if necessary)
        configuration.setAllowCredentials(true);

        // Set up the source with the configuration
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}