package com.noc.incidentservice.client;

import com.noc.incidentservice.dto.CreateNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Calls auth-service's internal notification-creation endpoint. */
@FeignClient(
        name = "auth-service",
              contextId = "notificationServiceClient"

)
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/internal")
    void create(@RequestBody CreateNotificationRequest request);
}