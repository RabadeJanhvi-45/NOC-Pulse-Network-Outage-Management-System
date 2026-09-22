package com.noc.alarmservice.exception;

/** Thrown when device-service can't be reached (down / not registered) to validate a deviceId. */
public class DeviceUnavailableException extends RuntimeException {

    public DeviceUnavailableException(String message) {
        super(message);
    }
}
