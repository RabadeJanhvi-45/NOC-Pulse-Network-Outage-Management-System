package com.noc.dashboardservice.exception;

/** Thrown when device-service, alarm-service or incident-service can't be reached (down, not registered, timeout) via Feign. */
public class UpstreamServiceUnavailableException extends RuntimeException {

    public UpstreamServiceUnavailableException(String message) {
        super(message);
    }
}
