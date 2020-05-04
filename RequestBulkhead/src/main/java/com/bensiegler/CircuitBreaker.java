package com.bensiegler;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.*;

public class CircuitBreaker {
    boolean serviceSuspended = false;

    int timeoutThreshHold;
    int serviceSuspensionSusceptibility;
    int healthCheckIntervalAfterSuspension;
    int healthCheckIntervalWhileActive;

    public ExecutorService executorService;
    HashMap<String, Long> failedRequest = new HashMap<>();

    public CircuitBreaker(int poolSize,
                          int timeoutThreshHold,
                          int serviceSuspensionSusceptibility,
                          int healthCheckIntervalAfterSuspension,
                          int healthCheckIntervalWhileActive) {
        this.timeoutThreshHold = timeoutThreshHold;
        this.serviceSuspensionSusceptibility = serviceSuspensionSusceptibility;
        this.healthCheckIntervalAfterSuspension = healthCheckIntervalAfterSuspension;
        this.healthCheckIntervalWhileActive = healthCheckIntervalWhileActive;

        executorService = Executors.newFixedThreadPool(poolSize);
    }

    public Future<?> submitRequest(Callable<?> callable, int id) throws TimeoutException, TrippedBreakerException {
        //TODO pull request ID off headers and store as key in hashmap.
        //TODO pull overall ID off headers.

        Long tracerId = 589549583842L;
        Properties properties  = BulkheadManager.getProperties("microservice.properties");

        String requestID  = properties.getProperty("servicename") + "65456543";

        long timeSent = System.currentTimeMillis();

        if(serviceSuspended) {
            throw new TrippedBreakerException("Request with id " + requestID + " could not be completed as a downstream service is unresponsive",requestID, tracerId);
        }

        Future<?> future = executorService.submit(callable);

        while(!future.isDone()) {
            long timeSoFar = System.currentTimeMillis() - timeSent;

            if(timeSoFar >= timeoutThreshHold) {
                //TODO emit event that request has timed out.
                failedRequest.put(requestID, System.currentTimeMillis());
                throw new TimeoutException("bulkhead has encountered a request (" + id +") that took longer than the allotted time.");
            }
        }
        return future;
    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "serviceSuspended=" + serviceSuspended +
                ", timeoutThreshHold=" + timeoutThreshHold +
                ", serviceSuspensionSusceptibility=" + serviceSuspensionSusceptibility +
                ", healthCheckIntervalAfterSuspension=" + healthCheckIntervalAfterSuspension;
    }


}
