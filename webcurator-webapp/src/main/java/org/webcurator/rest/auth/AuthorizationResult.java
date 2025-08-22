package org.webcurator.rest.auth;

public class AuthorizationResult {
    public boolean failed;
    public int status;
    public String message;

    public AuthorizationResult(boolean failed, int status, String message) {
        this.failed = failed;
        this.status = status;
        this.message = message;
    }
}
