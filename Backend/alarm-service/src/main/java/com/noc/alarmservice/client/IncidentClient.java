package com.noc.alarmservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client into incident-service's internal-only incident-creation
 * endpoint. Called synchronously right after a new Alarm is saved (see
 * AlarmServiceImpl.raiseAlarm) — not called on a deduplicated alarm, since
 * that already has an incident from its first occurrence.
 */
@FeignClient(name = "incident-service")
public interface IncidentClient {

    @PostMapping("/api/incidents/internal/from-alarm")
    void createFromAlarm(@RequestBody CreateIncidentFromAlarmRequest request);
}