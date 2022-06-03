package com.img.imgbackend;

import com.img.imgbackend.utils.GradientData;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSpecificDataTTest {
    @Test
    public void encapsulationTest() {
        int threadID = 0;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1);
        Lock lock  = new ReentrantLock();
        int NUM_THREADS = 1;
        GradientData gradientData = new GradientData(4, 4, NUM_THREADS);

        ThreadSpecificDataT capsule = new ThreadSpecificDataT(threadID
                , cyclicBarrier
                , NUM_THREADS
                , gradientData
        );
        assertEquals (threadID, capsule.threadID);
        assertEquals(cyclicBarrier, capsule.barrier);
        assertEquals(NUM_THREADS, capsule.NUM_THREADS);
        assertEquals(gradientData, capsule.gradientData);
    }

}
