package com.noc.dashboardservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT-based, role-based security. Tokens are issued by auth-service and
 * validated here via JwtAuthFilter (shared HS256 secret — see Backend
 * Guide §1.3/§1.7). This service is entirely read-only (no entities of
 * its own), so every authenticated user with an active role may reach
 * any endpoint.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/dashboard", "/api/dashboard/**", "/api/reports", "/api/reports/**")
                        .hasAnyRole("ADMIN", "NOC_OPERATOR", "ENGINEER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(RestAuthErrorHandlers.unauthorizedEntryPoint())
                        .accessDeniedHandler(RestAuthErrorHandlers.forbiddenHandler())
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
