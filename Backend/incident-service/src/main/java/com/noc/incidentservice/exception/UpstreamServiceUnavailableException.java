package com.noc.incidentservice.exception;

/** Thrown when device-service or alarm-service can't be reached (down, not registered, timeout) via Feign. */
public class UpstreamServiceUnavailableException extends RuntimeException {

    public UpstreamServiceUnavailableException(String message) {
        super(message);
    }
}
