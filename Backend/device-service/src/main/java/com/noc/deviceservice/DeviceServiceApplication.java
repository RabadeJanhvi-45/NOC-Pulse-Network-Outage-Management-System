package com.noc.deviceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * device-service — owns the device inventory for the Network Outage Dashboard:
 * registration, editing, status/health tracking and full change history.
 *
 * Foundational service — has no dependency on any other business service.
 * alarm-service and incident-service call INTO this service (via Feign)
 * to validate that a deviceId exists; this service never calls out.
 *
 * Port: 8082 | DB: device-db (H2, file-based)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}
