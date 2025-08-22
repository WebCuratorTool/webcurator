package org.webcurator.rest.auth;

public class AuthorizationException extends Exception {
    private final int status;

    public AuthorizationException(String message, int status) {
        super(message);
        this.status = status;
    }


    public int getStatus() {
        return status;
    }
}
