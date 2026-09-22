package com.noc.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Registry for the Network Outage Dashboard backend.
 * All other services (api-gateway, auth-service, device-service,
 * alarm-service, incident-service, dashboard-reporting-service)
 * register themselves here so they can discover and call each other
 * by application name instead of hardcoded host:port.
 *
 * Dashboard UI available at: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
