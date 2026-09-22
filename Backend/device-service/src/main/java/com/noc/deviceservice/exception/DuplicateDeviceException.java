package com.noc.deviceservice.exception;

/** Thrown when a device is registered with a deviceId that already exists (US-04 AC). */
public class DuplicateDeviceException extends RuntimeException {

    public DuplicateDeviceException(String message) {
        super(message);
    }
}
