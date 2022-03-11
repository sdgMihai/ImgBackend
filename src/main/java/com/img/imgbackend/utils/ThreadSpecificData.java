package com.img.imgbackend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ThreadSpecificData {
    int thread_id;
    Barrier barrier;
    Object lock;
    Image image;
    Image newImage;
    int nrFilters;
    int NUM_THREADS;
    List<String> filters;
}
