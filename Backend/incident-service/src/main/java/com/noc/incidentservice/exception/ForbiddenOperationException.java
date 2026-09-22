package com.noc.incidentservice.exception;

/** Thrown when the caller isn't allowed to perform this operation on this incident (e.g. an Engineer acting on someone else's incident, or an invalid state transition). */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}