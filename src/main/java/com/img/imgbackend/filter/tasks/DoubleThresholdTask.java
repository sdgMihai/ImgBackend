package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.utils.Image;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RecursiveAction;

@Builder
@Slf4j
public class DoubleThresholdTask extends RecursiveAction {
    private Image input;
    private Image output;
    private int start;
    private int stop;
    private int THRESHOLD;
    private final float thresholdHigh = 0.06f;
    private final float thresholdLow = 0.05f;

    @Override
    protected void compute() {
        DoubleThresholdTaskOne taskOne = DoubleThresholdTaskOne.builder()
                .input(input)
                .output(output)
                .start(start)
                .stop(stop)
                .THRESHOLD(THRESHOLD)
                .build();

        taskOne.fork();
        final Double maxVal = taskOne.join();
        float high = maxVal.floatValue() * thresholdHigh;
        float low = high * this.thresholdLow;
        log.debug(String.format("max %f, high %f, low %f\n", maxVal, high, low));

        DoubleThresholdTaskTwo taskTwo = DoubleThresholdTaskTwo.builder()
                .input(input)
                .output(output)
                .start(start)
                .stop(stop)
                .high(high)
                .low(low)
                .THRESHOLD(THRESHOLD)
                .build();

        taskTwo.fork();
        taskTwo.join();
    }
}
