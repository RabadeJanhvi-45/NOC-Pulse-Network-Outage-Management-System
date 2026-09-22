package com.noc.incidentservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT-based, role-based security. Tokens are issued by auth-service and
 * validated here via JwtAuthFilter.
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
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/incidents/internal", "/api/incidents/internal/**")
                        .hasRole("NOC_OPERATOR")
                        .requestMatchers(HttpMethod.GET, "/api/incidents", "/api/incidents/**")
                        .hasAnyRole("ADMIN", "NOC_OPERATOR", "ENGINEER")
                        .requestMatchers(HttpMethod.POST, "/api/incidents/*/start", "/api/incidents/*/resolve", "/api/incidents/*/reject-assignment", "/api/incidents/*/notes")
                        .hasRole("ENGINEER")
                        .requestMatchers(HttpMethod.POST, "/api/incidents/*/verify", "/api/incidents/*/reject-resolution")
                        .hasRole("NOC_OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/incidents/*/decide-rejection")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(RestAuthErrorHandlers.unauthorizedEntryPoint())
                        .accessDeniedHandler(RestAuthErrorHandlers.forbiddenHandler())
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
