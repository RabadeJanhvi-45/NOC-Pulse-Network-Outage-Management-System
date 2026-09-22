package com.noc.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Single entry point for all client traffic into the Network Outage
 * Dashboard backend. Routes requests to the correct downstream
 * microservice based on path prefix (see application.yml), resolving
 * the target instance dynamically via Eureka + client-side load
 * balancing (lb://service-name).
 *
 * Runs on port 8080.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
