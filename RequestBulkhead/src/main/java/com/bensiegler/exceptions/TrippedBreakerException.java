package com.bensiegler.exceptions;

import com.muttsapp.*;

public class TrippedBreakerException extends BulkheadException {
    public TrippedBreakerException(String message, String requestId, Long tracerId) {
        super(message, requestId, tracerId);
    }
}
