package com.bensiegler;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class Test {

    public static void main(String[] args) throws TrippedBreakerException, InterruptedException{
        BulkheadManager manager = new BulkheadManager();
        HashMap<String, Bulkhead> bulks = manager.bulkheads;

        Callable<Integer> callable1 = () -> {
            Thread.sleep(20);
            System.out.println("callable 1 completed");
            return 5;
        };

        Callable<Integer> callable2 = () -> {
            Thread.sleep(200);
            System.out.println("callable 2 completed");
            return 7;
        };

        Runnable runnable1 = () -> {
            try {
                bulks.get("hello").submitRequest(callable1, 4);
            }catch(TimeoutException | TrippedBreakerException e) {
                System.out.println(e.getMessage());
            }
        };

        Runnable runnable2 = () -> {
            try {
                bulks.get("serviceName").submitRequest(callable2, 4);
            }catch(TimeoutException | TrippedBreakerException e) {
                System.out.println(e.getMessage());
            }
        };

        long startcall1 = System.currentTimeMillis();
        for(int i = 0; i < 50; i++) {
            Thread t = new Thread(runnable1);
            t.start();
            t.join();;
        }
        System.out.println("callable 1 took " + (System.currentTimeMillis() - startcall1));
        Thread.sleep(2000);
        long startcall2 = System.currentTimeMillis();
        for(int i = 0; i < 50; i++) {
            Thread t = new Thread(runnable2);
            t.start();
            t.join();;
        }
        System.out.println("callable 2 took " + (System.currentTimeMillis() - startcall2));
        manager.shutdownAll();


    }
}
