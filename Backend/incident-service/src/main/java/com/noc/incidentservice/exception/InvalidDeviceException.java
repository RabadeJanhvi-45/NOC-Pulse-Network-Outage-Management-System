package com.noc.incidentservice.exception;

/** Thrown when device-service (via Feign) can't confirm the given deviceId exists. */
public class InvalidDeviceException extends RuntimeException {

    public InvalidDeviceException(String message) {
        super(message);
    }
}
