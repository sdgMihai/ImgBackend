package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.filter.SecondThresholdFilter;
import com.img.imgbackend.utils.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
@Builder
public class DoubleThresholdTaskTwo extends RecursiveAction {
    private Image input;
    private Image output;
    private int start;
    private int stop;
    private int THRESHOLD;
    private float low;
    private float high;

    @Override
    protected void compute() {
//        System.out.println("start task two");

        if (stop - start > THRESHOLD) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            processing();
        }
    }

    private List<DoubleThresholdTaskTwo> createSubtasks() {
        List<DoubleThresholdTaskTwo> subtasks = new ArrayList<>();

        int middle = (start + stop) / 2;

        if (middle > start)
            subtasks.add(DoubleThresholdTaskTwo.builder()
                    .input(input)
                    .output(output)
                    .start(start)
                    .stop(middle)
                    .high(high)
                    .low(low)
                    .THRESHOLD(THRESHOLD)
                    .build());
        if (middle + 1 < stop)
            subtasks.add(DoubleThresholdTaskTwo.builder()
                    .input(input)
                    .output(output)
                    .start(middle)
                    .stop(stop)
                    .high(high)
                    .low(low)
                    .THRESHOLD(THRESHOLD)
                    .build()
            );

        return subtasks;
    }

    private void processing() {
        SecondThresholdFilter.applyFilter(input, output, start, stop, high, low);
    }

}
