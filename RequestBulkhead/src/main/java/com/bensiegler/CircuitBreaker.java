package com.bensiegler;

import com.bensiegler.exceptions.TrippedBreakerException;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.*;

public class CircuitBreaker {
    boolean serviceSuspended = false;

    int poolSize;
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
        this.poolSize = poolSize;
        this.timeoutThreshHold = timeoutThreshHold;
        this.serviceSuspensionSusceptibility = serviceSuspensionSusceptibility;
        this.healthCheckIntervalAfterSuspension = healthCheckIntervalAfterSuspension;
        this.healthCheckIntervalWhileActive = healthCheckIntervalWhileActive;

        executorService = Executors.newFixedThreadPool(poolSize);
    }

    public static CircuitBreaker createNewBreakerFromExisting(CircuitBreaker circuitBreaker) {

        return new CircuitBreaker(circuitBreaker.poolSize,
                circuitBreaker.timeoutThreshHold,
                circuitBreaker.serviceSuspensionSusceptibility,
                circuitBreaker.healthCheckIntervalAfterSuspension,
                circuitBreaker.healthCheckIntervalWhileActive);
    }

    public Future<?> submitRequest(Callable<?> callable, Long tracerId) throws TimeoutException, TrippedBreakerException {
        tracerId = 589549583842L;

        Properties properties  = BulkheadManager.getProperties("microservice.properties");
        String requestID  = properties.getProperty("servicename") + NumberGenerators.randomNumberWithFixedLength(8);

        if(serviceSuspended) {
            failedRequest.put(requestID, System.currentTimeMillis());
            throw new TrippedBreakerException("Request with id " + requestID + " could not be completed as a downstream service is unresponsive",requestID, tracerId);
        }

        long timeSent = System.currentTimeMillis();
        Future<?> future = executorService.submit(callable);

        while(!future.isDone()) {
            long timeSoFar = System.currentTimeMillis() - timeSent;

            if(timeSoFar >= timeoutThreshHold) {
                //TODO emit event that request has timed out.
                failedRequest.put(requestID, System.currentTimeMillis());
                throw new TimeoutException("bulkhead has encountered a request with tracer id " + tracerId +" and outbound request id " + requestID +" that took longer than the allotted time.");
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
