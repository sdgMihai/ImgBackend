package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.filter.FirstThresholdFilter;
import com.img.imgbackend.utils.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

@Builder
@AllArgsConstructor
public class DoubleThresholdTaskOne  extends RecursiveTask<Double> {
    private Image input;
    private Image output;
    private int start;
    private int stop;
    private int THRESHOLD;

    @Override
    protected Double compute() {
        if (stop - start > THRESHOLD) {
            return ForkJoinTask.invokeAll(createSubtasks())
                    .stream()
                    .mapToDouble(ForkJoinTask::join)
                    .max().getAsDouble();
        } else {
            return processing();
        }
    }

    private Collection<DoubleThresholdTaskOne> createSubtasks() {
        List<DoubleThresholdTaskOne> dividedTasks = new ArrayList<>();
        int middle = (start + stop) / 2;
        if (middle > start)
            dividedTasks.add(new DoubleThresholdTaskOne(
                input, output, start, middle, THRESHOLD));
        if (middle + 1 < stop)
            dividedTasks.add(new DoubleThresholdTaskOne(
                    input, output, middle, stop, THRESHOLD));
        return dividedTasks;
    }

    private Double processing() {
        return FirstThresholdFilter.applyFilter(input, output, start, stop);
    }
}
