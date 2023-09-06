package org.webcurator.rest;

public class BadRequestError extends Exception {
    public BadRequestError(String msg) {
        super(msg);
    }
}
