package com.noc.incidentservice.client;

import com.noc.incidentservice.dto.AlarmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Calls alarm-service by its Eureka-registered application name
 * ("alarm-service") to fetch alarm details when an incident is created
 * from an alarm (guide §5.3). Per the cross-service communication rule,
 * incident-service never queries alarm-db directly.
 */
@FeignClient(name = "alarm-service")
public interface AlarmServiceClient {

    @GetMapping("/api/alarms/{id}")
    AlarmResponse getAlarmById(@PathVariable("id") Long id);

    @org.springframework.web.bind.annotation.PostMapping("/api/alarms/{id}/clear")
    AlarmResponse clearAlarm(@PathVariable("id") Long id);
}
