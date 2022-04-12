package com.img.imgbackend.utils;


import com.img.imgbackend.filter.FilterAdditionalData;
import lombok.AllArgsConstructor;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;

@AllArgsConstructor
public class ThreadSpecificDataT implements FilterAdditionalData {
    public int threadID;
    public CyclicBarrier barrier;
    public int NUM_THREADS;
}
