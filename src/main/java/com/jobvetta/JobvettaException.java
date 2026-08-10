package com.jobvetta;

import com.fasterxml.jackson.databind.JsonNode;

/** Error returned while configuring or calling Jobvetta. */
public final class JobvettaException extends RuntimeException {
    private final Integer statusCode;
    private final JsonNode body;

    JobvettaException(String message) {
        this(message, null, null, null);
    }

    JobvettaException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    JobvettaException(String message, Integer statusCode, JsonNode body) {
        this(message, statusCode, body, null);
    }

    private JobvettaException(
            String message, Integer statusCode, JsonNode body, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.body = body;
    }

    /** HTTP response status, or {@code null} when no response was received. */
    public Integer statusCode() {
        return statusCode;
    }

    /** Parsed error body, or {@code null} when no response was received. */
    public JsonNode body() {
        return body;
    }
}
