package com.bensiegler;

import com.bensiegler.circuitbreaker.CircuitBreaker;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;

public class BulkheadManager {
    HashMap<String, Bulkhead> bulkheads = new HashMap<>();

    public BulkheadManager() {
        bulkheads = new HashMap<>();
        addConfigBulkheads();
    }

    public static void main(String[] args) {
        BulkheadManager manager = new BulkheadManager();
        manager.addConfigBulkheads();
        HashMap<String, Bulkhead> bulks = manager.bulkheads;

        System.out.println(bulks.get("hello"));
        System.out.println(bulks.get("serviceName").toString());

    }

    private void addConfigBulkheads() {
        String propFileName = "config.properties";
        try(InputStream inputStream = BulkheadManager.class.getClassLoader().getResourceAsStream(propFileName)) {
            Properties prop = new Properties();


            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            Object[] keyArray = prop.keySet().toArray();
            String currentBulkhead = "nothing";
            String currentProp;

            ArrayList<String> constructedBulkheads = new ArrayList<>();
            try {
                for (int i = 0; i < keyArray.length; i++) {
                    currentProp = String.valueOf(keyArray[i]);
                    if(currentProp.substring(0, currentProp.indexOf(".")).equals("bulkhead")) {

                        currentProp = currentProp.substring(currentProp.indexOf(".") + 1);
                        currentBulkhead = currentProp.substring(0, currentProp.indexOf("."));

                        if(!constructedBulkheads.contains(currentBulkhead)) {
                            String location = prop.getProperty("bulkhead." + currentBulkhead + ".location");
                            int poolSize = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead + ".poolSize"));
                            int timeoutThreshold = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead + ".circuitBreaker.timeoutThreshHold"));
                            int serviceSuspensionSusceptibility = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead + ".circuitBreaker.serviceSuspensionSusceptibility"));
                            int healthCheckIntervalAfterSuspension = Integer.parseInt(
                                    prop.getProperty("bulkhead." + currentBulkhead + ".circuitBreaker.healthCheckIntervalAfterSuspension"));

                            CircuitBreaker circuitBreaker = new CircuitBreaker(timeoutThreshold,
                                                                                serviceSuspensionSusceptibility,
                                                                                healthCheckIntervalAfterSuspension);

                            addNewBulkhead(currentBulkhead, location, poolSize, circuitBreaker);
                            constructedBulkheads.add(currentBulkhead);
                        }
                    }
                }
            }catch(Exception e) {
                System.err.println("Malformed Bulkhead Config File");
                e.printStackTrace();
            }

        }catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private void addNewBulkhead(String name, String location, int poolSize, CircuitBreaker circuitBreaker) {
        bulkheads.put(name, new Bulkhead(name, location, poolSize, circuitBreaker));
    }

    public void refreshBulkheadConfig() {
        bulkheads.clear();
        addConfigBulkheads();
    }
}
