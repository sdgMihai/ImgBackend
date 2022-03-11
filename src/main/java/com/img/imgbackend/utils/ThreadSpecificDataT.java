package com.img.imgbackend.utils;


import com.img.imgbackend.filter.FilterAdditionalData;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ThreadSpecificDataT implements FilterAdditionalData {
    public int threadID;
    public Barrier barrier;
    public final Object mutex;
    public int NUM_THREADS;
}
