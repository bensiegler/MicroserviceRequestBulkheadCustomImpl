package com.bensiegler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class BulkheadManager {
    HashMap<String, Bulkhead> bulkheads;

    public BulkheadManager() {
        bulkheads = new HashMap<>();
        addConfigBulkheads();
    }

    private void addConfigBulkheads() {
            Properties prop = getProperties("bulkhead.properties");
            Object[] keyArray = prop.keySet().toArray();
            String currentBulkhead;
            String currentProp;

            ArrayList<String> constructedBulkheads = new ArrayList<>();
            try {
                for (Object o : keyArray) {
                    currentProp = String.valueOf(o);
                    if (currentProp.substring(0, currentProp.indexOf(".")).equals("bulkhead")) {

                        currentProp = currentProp.substring(currentProp.indexOf(".") + 1);
                        currentBulkhead = currentProp.substring(0, currentProp.indexOf("."));

                        if (!constructedBulkheads.contains(currentBulkhead)) {
                            String[] location = prop.getProperty("bulkhead." + currentBulkhead + ".locations").split(",");
                            int poolSize = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead +
                                            ".poolSize"));
                            int timeoutThreshold = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead +
                                            ".circuitBreaker.timeoutThreshHold"));
                            int serviceSuspensionSusceptibility = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead +
                                            ".circuitBreaker.serviceSuspensionSusceptibility"));
                            int healthCheckIntervalAfterSuspension = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead +
                                            ".circuitBreaker.healthCheckIntervalAfterSuspension"));
                            int healthCheckIntervalWhileActive = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead +
                                            ".circuitBreaker.healthCheckIntervalWhileActive"));

                            CircuitBreaker circuitBreaker = new CircuitBreaker(poolSize, timeoutThreshold,
                                    serviceSuspensionSusceptibility,
                                    healthCheckIntervalAfterSuspension,
                                    healthCheckIntervalWhileActive);

                            addNewBulkhead(currentBulkhead, location, circuitBreaker);
                            constructedBulkheads.add(currentBulkhead);
                        }
                    }
                }
            }catch(Exception e) {
                System.err.println("Malformed Bulkhead Config File");
                e.printStackTrace();
            }
    }

    private void addNewBulkhead(String name, String[] location, CircuitBreaker circuitBreaker) {
        bulkheads.put(name, new Bulkhead(name, location, circuitBreaker));
    }

    //stop all current bulkheads from accepting requests but allow them to complete
    //queued tasks. Then spins up new bulkheads from config file.
    public void refreshBulkheadConfig() {
        shutdownAll();
        bulkheads.clear();
        addConfigBulkheads();
    }

    public void shutdownAll() {
        Object[] keys = bulkheads.keySet().toArray();

        for(Object o: keys) {
           CircuitBreaker[] circuitBreakers = bulkheads.get(String.valueOf(o)).getCircuitBreakers();
           for(CircuitBreaker cb: circuitBreakers) {
               cb.executorService.shutdown();
           }
        }
    }

    public void shutdownAllNow() {
        Object[] keys = bulkheads.keySet().toArray();

        for(Object o: keys) {
            CircuitBreaker[] circuitBreakers = bulkheads.get(String.valueOf(o)).getCircuitBreakers();
            for(CircuitBreaker cb: circuitBreakers) {
                cb.executorService.shutdownNow();
            }
        }
    }

    public static Properties getProperties(String propFileName) {
        Properties prop = new Properties();
        try(InputStream inputStream = BulkheadManager.class.getClassLoader().getResourceAsStream(propFileName)) {
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        }catch (Exception e) {
            //Todo change to logger instead of printing
            System.out.println("Exception: " + e);
        }
        return prop;

    }

}
