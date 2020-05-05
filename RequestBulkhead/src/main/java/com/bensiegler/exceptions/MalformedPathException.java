package com.bensiegler.exceptions;

public class MalformedPathException extends BulkheadException{
    public MalformedPathException(String message, String requestId, Long tracerId) {
        super(message, requestId, tracerId);
    }
}
