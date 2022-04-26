package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.filter.FirstGradientFilter;
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
public class GradientTaskOne  extends RecursiveTask<Double> {
    private final Image input;
    private final int start;
    private final int stop;
    private final int THRESHOLD;
    private final float[][] theta;
    private final float[][] ix;
    private final float[][] iy;

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

    private Collection<GradientTaskOne> createSubtasks() {
        List<GradientTaskOne> dividedTasks = new ArrayList<>();
        int middle = (start + stop) / 2;
        if (middle > start)
            dividedTasks.add(GradientTaskOne.builder()
                    .input(input)
                    .start(start)
                    .stop(middle)
                    .THRESHOLD(THRESHOLD)
                    .theta(theta)
                    .ix(ix)
                    .iy(iy)
                    .build());
        if (middle + 1 < stop)
            dividedTasks.add(GradientTaskOne.builder()
                    .input(input)
                    .start(middle)
                    .stop(stop)
                    .THRESHOLD(THRESHOLD)
                    .theta(theta)
                    .ix(ix)
                    .iy(iy)
                    .build());
        return dividedTasks;
    }

    private Double processing() {
        return new FirstGradientFilter(theta, ix, iy)
                .applyFilter(input, start, stop);
    }
}
