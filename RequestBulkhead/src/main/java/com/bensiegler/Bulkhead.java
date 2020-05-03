package com.bensiegler;

import java.util.concurrent.*;

public class Bulkhead {
    private String name;
    private String[] location;
    private CircuitBreaker circuitBreaker;

    public Bulkhead(String name, String[] location, CircuitBreaker circuitBreaker) {
        this.name = name;
        this.location = location;
        this.circuitBreaker = circuitBreaker;
    }

    public Future<?> submitRequest(Callable<?> callable, int id) throws TimeoutException, TrippedBreakerException{
        return circuitBreaker.submitRequest(callable, id);
    }

    public String getName() {
        return name;
    }

    public String[] getLocation() {
        return location;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    @Override
    public String toString() {
        return "Bulkhead{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", circuitBreaker=" + circuitBreaker.toString() +
                '}';
    }
}
