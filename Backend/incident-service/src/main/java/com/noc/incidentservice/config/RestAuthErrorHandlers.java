package com.noc.incidentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noc.incidentservice.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.time.LocalDateTime;

/**
 * Turns "no/invalid token" and "valid token, wrong role" into the same
 * {@code ErrorResponse} JSON shape the rest of the service's controllers
 * already return (see GlobalExceptionHandler), instead of Spring Security's
 * default empty 401/403 body.
 */
public final class RestAuthErrorHandlers {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private RestAuthErrorHandlers() {
    }

    public static AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                write(response, HttpStatus.UNAUTHORIZED, "Missing or invalid authentication token");
    }

    public static AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) ->
                write(response, HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    private static void write(HttpServletResponse response, HttpStatus status, String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .fieldErrors(null)
                .build();
        response.getWriter().write(MAPPER.writeValueAsString(body));
    }
}
