package com.spicep.cryptowallet.exception;

import lombok.Data;

/**
 * Class that represents the error response.
 */
@Data
public class ErrorResponse {
    private int status;
    private String message;
    private long timestamp;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}