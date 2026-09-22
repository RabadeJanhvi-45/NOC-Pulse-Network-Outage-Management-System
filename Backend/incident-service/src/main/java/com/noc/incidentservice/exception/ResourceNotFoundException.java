package com.noc.incidentservice.exception;

/** Thrown when a requested Incident (or related child resource) doesn't exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
