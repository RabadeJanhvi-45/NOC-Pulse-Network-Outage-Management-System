package com.noc.alarmservice.config;

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
 * Guide §1.3/§1.7). Any authenticated user may read alarms and severity
 * rules; raising/acknowledging alarms needs ADMIN or NOC_OPERATOR;
 * creating/editing severity rules is ADMIN-only since it changes how
 * every future alarm gets classified system-wide.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/alarms/rules", "/api/alarms/rules/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/alarms/rules/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/alarms/rules/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/alarms", "/api/alarms/**")
                                                .hasAnyRole("ADMIN", "NOC_OPERATOR", "ENGINEER")
                                                .requestMatchers("/api/alarms", "/api/alarms/**").hasAnyRole("ADMIN", "NOC_OPERATOR")
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(
                                                                RestAuthErrorHandlers.unauthorizedEntryPoint())
                                                .accessDeniedHandler(RestAuthErrorHandlers.forbiddenHandler()))
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}
