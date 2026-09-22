package com.noc.authservice.exception;

/** Thrown on failed login (bad username, bad password, or disabled account). */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
