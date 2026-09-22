package com.noc.alarmservice.exception;

/** Thrown when an alarm is raised against a device whose status isn't Active. */
public class DeviceNotActiveException extends RuntimeException {

    public DeviceNotActiveException(String message) {
        super(message);
    }
}