package com.bensiegler.circuitbreaker;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CircuitBreaker {

    boolean serviceSuspended = false;

    int timeoutThreshHold;
    int serviceSuspensionSusceptibility;
    int healthCheckIntervalAfterSuspension;

    ExecutorService executorService;


    HashMap<Integer, Long> requests = new HashMap<Integer, Long>();

    public CircuitBreaker(int timeoutThreshHold, int serviceSuspensionSusceptibility, int healthCheckIntervalAfterSuspension) {
        this.timeoutThreshHold = timeoutThreshHold;
        this.serviceSuspensionSusceptibility = serviceSuspensionSusceptibility;
        this.healthCheckIntervalAfterSuspension = healthCheckIntervalAfterSuspension;
    }

    public Future<Object> sumbitRequest(Callable<Object> callable) {

    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "serviceSuspended=" + serviceSuspended +
                ", timeoutThreshHold=" + timeoutThreshHold +
                ", serviceSuspensionSusceptibility=" + serviceSuspensionSusceptibility +
                ", healthCheckIntervalAfterSuspension=" + healthCheckIntervalAfterSuspension +
                ", requests=" + requests +
                '}';
    }


}
