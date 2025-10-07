package org.webcurator.rest.common;

//{"timestamp":"2025-10-07T23:24:47.461+00:00","status":400,"error":"Bad Request","path":"/wct/api/v1/targets/5t"}
public class BadResponseError {
    private String timestamp;
    private int status;
    private String error;
    private String path;

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
}
