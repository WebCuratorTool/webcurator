package org.webcurator.rest.common;

public class BadRequestError extends Exception {
    public BadRequestError(String msg) {
        super(msg);
    }
}
