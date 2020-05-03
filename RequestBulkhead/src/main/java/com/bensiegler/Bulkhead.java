package com.bensiegler;

import com.bensiegler.circuitbreaker.CircuitBreaker;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

public class Bulkhead {

    //config fields
    String name;
    String location;
    int poolSize;

    CircuitBreaker circuitBreaker;



    public Bulkhead(String name, String location, int poolSize, CircuitBreaker circuitBreaker) {
        this.name = name;
        this.location = location;
        this.poolSize = poolSize;
        this.circuitBreaker = circuitBreaker;

        executorService = Executors.newFixedThreadPool(poolSize);
    }



    @Override
    public String toString() {
        return "Bulkhead{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", poolSize=" + poolSize +
                ", circuitBreaker=" + circuitBreaker.toString() +
                '}';
    }
}
