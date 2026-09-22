package com.noc.incidentservice.exception;

/** Thrown when alarm-service (via Feign) can't confirm the given alarmId exists. */
public class InvalidAlarmException extends RuntimeException {

    public InvalidAlarmException(String message) {
        super(message);
    }
}
