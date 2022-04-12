package com.img.imgbackend;

import com.img.imgbackend.utils.AtomicFloat;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtomicFloatTest {
    @Test
    public void constructorTest() {
        AtomicFloat atomicFloat = new AtomicFloat();
        assertEquals(0.f, atomicFloat.get());

        AtomicFloat atomicFloat1 = new AtomicFloat(1.4f);
        assertEquals(1.4f, atomicFloat1.get());
    }

    /*
    Find the max value in an array of doubles.
     */
    @Test
    public void testCompareAndSet() {
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        AtomicFloat atomicFloat = new AtomicFloat(0);

        List<Callable<Object>> tasks = new ArrayList<>();
        Random random = new Random();
        final double[] doubles = random.doubles().limit(1000).toArray();
        final OptionalDouble max = Arrays.stream(doubles).max();

        for (double v : doubles) {
            Runnable c = () -> atomicFloat.applyMax((float) v);
            tasks.add(Executors.callable(c));
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        try {
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(max.isPresent()) {
            assertEquals((float)max.getAsDouble(), atomicFloat.get());
        }
    }
}
