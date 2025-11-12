package org.webcurator.rest.auth;

public class InvalidSessionException extends Exception {
    public InvalidSessionException(String id) {
        super(String.format("Session with %s has expired", id));
    }
}
