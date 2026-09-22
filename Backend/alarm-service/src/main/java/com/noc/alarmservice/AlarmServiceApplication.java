package com.noc.alarmservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * alarm-service — captures raw alarms raised against devices, classifies
 * severity (via SeverityRule matching, with a fallback default), groups
 * duplicate alarms from the same fault, and supports acknowledgement.
 *
 * Depends on device-service (via Feign) to confirm a deviceId exists
 * before an alarm is saved. incident-service calls INTO this service.
 *
 * Port: 8083 | DB: alarm-db (H2, file-based)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AlarmServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlarmServiceApplication.class, args);
    }
}
