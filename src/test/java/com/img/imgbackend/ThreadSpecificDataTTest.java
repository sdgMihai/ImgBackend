package com.img.imgbackend;

import com.img.imgbackend.utils.Barrier;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSpecificDataTTest {
    @Test
    public void encapsulationTest() {
        int threadID = 0;
        Barrier cyclicBarrier = new Barrier(1);
        Lock lock  = new ReentrantLock();
        int NUM_THREADS = 1;

        ThreadSpecificDataT capsule = new ThreadSpecificDataT(threadID
                , cyclicBarrier
                , lock
                , NUM_THREADS
        );
        assertEquals (threadID, capsule.threadID);
        assertEquals(cyclicBarrier, capsule.barrier);
        assertEquals(lock, capsule.mutex);
        assertEquals(NUM_THREADS, capsule.NUM_THREADS);
    }

}
