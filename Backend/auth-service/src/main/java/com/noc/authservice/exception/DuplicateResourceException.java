package com.noc.authservice.exception;

/** Thrown when a username / role name / permission name that must be unique already exists. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
