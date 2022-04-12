package com.img.imgbackend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;

@Data
@AllArgsConstructor
public class ThreadSpecificData {
    int thread_id;
    CyclicBarrier barrier;
    Image image;
    Image newImage;
    int nrFilters;
    int NUM_THREADS;
    List<String> filters;
}
