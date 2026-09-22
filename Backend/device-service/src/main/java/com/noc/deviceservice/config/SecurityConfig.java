package com.noc.deviceservice.config;

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
 * validated here via JwtAuthFilter (shared HS256 secret — see Backend
 * Guide §1.3/§1.7). Any authenticated user (ADMIN, NOC_OPERATOR, ENGINEER)
 * may read device data; only ADMIN and NOC_OPERATOR may register, edit,
 * or remove devices.
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
                        .requestMatchers(HttpMethod.GET, "/api/devices", "/api/devices/**")
                        .hasAnyRole("ADMIN", "NOC_OPERATOR", "ENGINEER")
                        .requestMatchers("/api/devices", "/api/devices/**").hasAnyRole("ADMIN", "NOC_OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/device-requests", "/api/device-requests/**")
                        .hasAnyRole("NOC_OPERATOR", "ADMIN")
                        .requestMatchers("/api/device-requests", "/api/device-requests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/device-type-specializations", "/api/device-type-specializations/**")
                        .authenticated()
                        .requestMatchers("/api/device-type-specializations", "/api/device-type-specializations/**").hasRole("ADMIN")
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
