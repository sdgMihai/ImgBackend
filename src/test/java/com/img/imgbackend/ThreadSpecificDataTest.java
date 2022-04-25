package com.img.imgbackend;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificData;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSpecificDataTest {
    @Test
    public void testEncapsulation() {
        int threadID = 0;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1);
        Lock lock  = new ReentrantLock();
        Image input = new Image(1, 1);
        Image output = new Image(1, 1);
        int nrFilters = 1;
        int NUM_THREADS = 1;
        String[] filterList = new String[0];
        ThreadSpecificData capsule = new ThreadSpecificData(threadID
                , cyclicBarrier
                , lock
                , input
                , output
                , nrFilters
                , NUM_THREADS
                , filterList
        );
        assertEquals (threadID, capsule.getThread_id());
        assertEquals(cyclicBarrier, capsule.getBarrier());
        assertEquals(lock, capsule.getLock());
        assertEquals(input, capsule.getImage());
        assertEquals(output, capsule.getNewImage());
        assertEquals(nrFilters, capsule.getNrFilters());
        assertEquals(NUM_THREADS, capsule.getNUM_THREADS());
        assertEquals(filterList, capsule.getFilters());
    }
}
