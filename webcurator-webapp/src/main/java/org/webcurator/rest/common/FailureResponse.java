package org.webcurator.rest.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

//{"timestamp":"2025-10-07T23:24:47.461+00:00","status":400,"error":"Bad Request","path":"/wct/api/v1/targets/5t"}
public class FailureResponse {
    private String timestamp;
    private int status;
    private String error;
    private String path;

    public FailureResponse() {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }

    public FailureResponse(String error) {
        this();
        this.error = error;
    }


    public FailureResponse(int status, String error) {
        this(error);
        this.status = status;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static ResponseEntity<?> error(HttpStatus status, String error) {
        FailureResponse rsp = new FailureResponse(status.value(), error);
        return ResponseEntity.status(status.value()).body(rsp);
    }
}
