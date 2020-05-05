package com.bensiegler.exceptions;

import com.muttsapp.MonitoredException;

public class BulkheadException extends MonitoredException {

    public BulkheadException(String message, String requestId, Long tracerId) {
        super(message, requestId, tracerId);
    }
}
