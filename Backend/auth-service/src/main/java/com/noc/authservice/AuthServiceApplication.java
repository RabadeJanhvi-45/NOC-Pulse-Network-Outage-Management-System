package com.noc.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * auth-service — login, JWT issuing, user management, and role/permission
 * management for the Network Outage Dashboard. Also records the audit trail
 * for logins, logouts and unauthorized-access attempts.
 *
 * Reference service (Backend Guide §2) — built first among the business
 * services, per the recommended build order (§7, step 3). Other services
 * validate JWTs issued here on their protected routes.
 *
 * Port: 8081 | DB: auth-db (H2, file-based)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
