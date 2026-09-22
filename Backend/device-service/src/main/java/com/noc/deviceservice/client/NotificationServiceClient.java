package com.noc.deviceservice.client;

import com.noc.deviceservice.dto.CreateNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "auth-service",
        contextId = "notificationServiceClient"
)
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/internal")
    void create(@RequestBody CreateNotificationRequest request);
}