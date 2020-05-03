package com.bensiegler;

import com.muttsapp.*;

public class TrippedBreakerException extends MonitoredException {
    public TrippedBreakerException(String message, String requestId, Long tracerId) {
        super(message, requestId, tracerId);
    }
}
