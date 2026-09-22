package com.noc.deviceservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Test-only token minting, signed with the same shared secret as
 * src/test/resources/application.yml's jwt.secret. Lets tests exercise
 * role-based access rules (ADMIN / NOC_OPERATOR / ENGINEER) without needing
 * a running auth-service or seeded users — this service only ever
 * validates tokens, it never issues them (see JwtAuthFilter).
 */
public final class JwtTestUtil {

    private static final String SECRET =
            "dev-only-change-me-network-outage-dashboard-auth-service-secret-key-2024";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtTestUtil() {
    }

    public static String tokenFor(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setIssuer("auth-service")
                .setSubject(username)
                .claim("userId", 1L)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 3_600_000))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String expiredTokenFor(String username, String role) {
        Date past = new Date(System.currentTimeMillis() - 10_000);
        return Jwts.builder()
                .setIssuer("auth-service")
                .setSubject(username)
                .claim("userId", 1L)
                .claim("role", role)
                .setIssuedAt(new Date(past.getTime() - 3_600_000))
                .setExpiration(past)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
