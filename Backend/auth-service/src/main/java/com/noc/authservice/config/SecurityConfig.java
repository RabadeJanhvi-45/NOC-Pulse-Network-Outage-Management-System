package com.noc.authservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/register/**").permitAll()
                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/users", "/api/users/**", "/api/roles", "/api/roles/**", "/api/audit-logs", "/api/audit-logs/**",
                                "/api/registration-requests", "/api/registration-requests/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/engineers/*/specialization")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/engineers/me", "/api/engineers/me/**")
                        .hasRole("ENGINEER")
                        .requestMatchers(HttpMethod.GET, "/api/engineers", "/api/engineers/**")
                        .authenticated()
                        .requestMatchers("/api/notifications", "/api/notifications/**").authenticated()
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