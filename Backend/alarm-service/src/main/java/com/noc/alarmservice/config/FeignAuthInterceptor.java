package com.noc.alarmservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the inbound request's {@code Authorization} bearer token onto
 * every outgoing Feign call this service makes to another service (e.g.
 * device-service, alarm-service). Without this, downstream services now
 * that they validate JWTs (see JwtAuthFilter) would reject these
 * service-to-service calls with 401, since a fresh Feign call otherwise
 * carries no auth header of its own.
 *
 * Only applies to calls made while handling a real inbound HTTP request
 * (there is no user token to forward from a non-request context, e.g. a
 * @Scheduled job — those calls stay unauthenticated Feign requests, and
 * should not target endpoints that require a role).
 */
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader)) {
            template.header("Authorization", authHeader);
        }
    }
}
