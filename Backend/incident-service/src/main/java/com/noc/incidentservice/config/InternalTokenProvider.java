package com.noc.incidentservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Mints a short-lived internal token for outbound Feign calls that don't
 * have a real inbound user request to forward a token from (e.g. the
 * @Scheduled UnassignedIncidentRetryScanner calling auth-service /
 * device-service). Signed with the same shared jwt.secret every service
 * already trusts (see FeignAuthInterceptor / JwtAuthFilter) — same
 * signature validation path a real user token goes through, just minted
 * locally instead of by auth-service.
 *
 * Role is ADMIN so it can reach every internal-facing endpoint the
 * assignment engine needs. This is a known simplification (no dedicated
 * service-account path yet — see README "Known gaps"); it's scoped as
 * tightly as the shared-secret model already in place allows.
 */
@Component
public class InternalTokenProvider {

    private static final String INTERNAL_SUBJECT = "incident-service-internal";
    private static final long TOKEN_TTL_MS = 60_000; // 1 minute — only needs to live long enough for one Feign call

    private final SecretKey signingKey;

    public InternalTokenProvider(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String mintInternalToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + TOKEN_TTL_MS);

        return Jwts.builder()
                .setSubject(INTERNAL_SUBJECT)
                .claim("role", "ADMIN")
                .claim("userId", 0L)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}