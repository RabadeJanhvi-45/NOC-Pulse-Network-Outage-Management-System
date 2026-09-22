package com.noc.incidentservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the inbound request's {@code Authorization} bearer token onto
 * every outgoing Feign call this service makes to another service (e.g.
 * device-service, alarm-service, auth-service). Without this, downstream
 * services that validate JWTs (see JwtAuthFilter) would reject these
 * service-to-service calls with 401.
 *
 * When there's no inbound request to forward from — the AssignmentEngine
 * running inside the @Scheduled UnassignedIncidentRetryScanner, most
 * notably — falls back to a locally minted internal token (see
 * InternalTokenProvider) instead of going out unauthenticated.
 */
@Component
@RequiredArgsConstructor
public class FeignAuthInterceptor implements RequestInterceptor {

    private final InternalTokenProvider internalTokenProvider;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String authHeader = null;
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            authHeader = request.getHeader("Authorization");
        }

        if (StringUtils.hasText(authHeader)) {
            template.header("Authorization", authHeader);
        } else {
            template.header("Authorization", "Bearer " + internalTokenProvider.mintInternalToken());
        }
    }
}