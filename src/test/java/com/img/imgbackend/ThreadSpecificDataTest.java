package com.img.imgbackend;

import com.img.imgbackend.utils.GradientData;
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
        GradientData gradientData = new GradientData(input.height, input.width, NUM_THREADS);
        String[] filterArr = new String[]{"black-white"};
        ThreadSpecificData capsule = new ThreadSpecificData(threadID
                , cyclicBarrier
                , input
                , output
                , nrFilters
                , NUM_THREADS
                , filterArr
                , gradientData
        );
        assertEquals (threadID, capsule.getThread_id());
        assertEquals(cyclicBarrier, capsule.getBarrier());
        assertEquals(input, capsule.getImage());
        assertEquals(output, capsule.getNewImage());
        assertEquals(nrFilters, capsule.getNrFilters());
        assertEquals(NUM_THREADS, capsule.getNUM_THREADS());
        assertEquals(filterArr, capsule.getFilters());
        assertEquals(gradientData, capsule.getGData());
    }
}
