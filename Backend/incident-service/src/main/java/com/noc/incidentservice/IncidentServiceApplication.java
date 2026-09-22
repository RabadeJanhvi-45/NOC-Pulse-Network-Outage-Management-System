package com.noc.incidentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * incident-service — the core workflow service. Turns alarms (or manual
 * reports) into trackable incidents, manages assignment, progress notes,
 * SLA tracking, and escalation.
 *
 * Depends on device-service (confirm deviceId) and alarm-service (fetch
 * alarm details when created from an alarm), both via Feign resolved
 * through Eureka. Per the cross-service communication rule, incident-service
 * never queries device-db or alarm-db directly.
 *
 * Port: 8084 | DB: incident-db (H2, file-based)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class IncidentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentServiceApplication.class, args);
    }
}
