package com.img.imgbackend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;

@Data
@AllArgsConstructor
public class ThreadSpecificData {
    int thread_id;
    CyclicBarrier barrier;
    Lock lock;
    Image image;
    Image newImage;
    int nrFilters;
    int NUM_THREADS;
    String[] filters;
}
