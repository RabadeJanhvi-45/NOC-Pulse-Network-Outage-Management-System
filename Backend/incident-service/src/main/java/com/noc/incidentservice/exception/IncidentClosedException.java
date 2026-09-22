package com.noc.incidentservice.exception;

/** Thrown when any write is attempted on a CLOSED incident — CLOSED is final and immutable. */
public class IncidentClosedException extends RuntimeException {
    public IncidentClosedException(String message) {
        super(message);
    }
}