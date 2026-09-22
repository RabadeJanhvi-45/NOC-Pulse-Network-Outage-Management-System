package com.noc.dashboardservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * dashboard-reporting-service — read-only aggregation layer for the
 * Network Outage Dashboard. Has no entities or database of its own;
 * every request pulls live data from device-service, alarm-service and
 * incident-service (via Feign, resolved through Eureka) and composes it
 * into dashboard widgets and reports on the fly.
 *
 * Depends on all three business services (Backend Guide §7, build step 7).
 * Kept stateless by design — easiest to scale, simplest to test.
 *
 * Port: 8085 | DB: none
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DashboardReportingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardReportingServiceApplication.class, args);
    }
}
