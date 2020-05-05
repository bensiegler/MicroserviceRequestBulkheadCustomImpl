package com.bensiegler;

import com.bensiegler.exceptions.TrippedBreakerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.concurrent.*;

public class Bulkhead {
    private final String name;
    private final String[] locations;
    private final CircuitBreaker[] circuitBreakerArr;

    @Autowired
    RestTemplate restTemplate;


    public Bulkhead(String name, String[] location, CircuitBreaker circuitBreakerTemplate) {
        this.name = name;
        this.locations = location;

        //initialize first breaker in array.
        this.circuitBreakerArr = new CircuitBreaker[location.length];
        this.circuitBreakerArr[0] = CircuitBreaker.createNewBreakerFromExisting(circuitBreakerTemplate);

    }

    public <T> Future<?> submitRequest(String pathToResource, Long tracerId, Class<T> clazz) throws TimeoutException, TrippedBreakerException {
        CircuitBreaker availableBreaker = null;
        String location = null;

        //get best available location
        for(int i = 0; i < circuitBreakerArr.length; i++) {
            if(!circuitBreakerArr[i].serviceSuspended) {
                availableBreaker = circuitBreakerArr[i];
                location = locations[i];
                break;
            }
        }



        String finalLocation = location;
        Callable<T> callable = () -> restTemplate.getForObject(finalLocation + pathToResource, clazz);

        //Submit if circuit breaker initialized. If not initialize and then submit
        try {
            return availableBreaker.submitRequest(callable, tracerId);
        }catch (NullPointerException e) {
            availableBreaker = CircuitBreaker.createNewBreakerFromExisting(circuitBreakerArr[0]);
            return availableBreaker.submitRequest(callable, tracerId);
        }
    }

    public String getName() {
        return name;
    }

    public String[] getLocations() {
        return locations;
    }

    public CircuitBreaker[] getCircuitBreakers() {
        return circuitBreakerArr;
    }

    @Override
    public String toString() {
        return "Bulkhead{" +
                "name='" + name + '\'' +
                ", location=" + Arrays.toString(locations) +
                ", circuitBreakerArr=" + Arrays.toString(circuitBreakerArr) +
                '}';
    }
}
