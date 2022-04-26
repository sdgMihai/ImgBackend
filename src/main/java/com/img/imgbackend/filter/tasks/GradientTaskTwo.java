package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.filter.SecondGradientFilter;
import com.img.imgbackend.utils.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
@Builder
public class GradientTaskTwo  extends RecursiveAction {
    private final Image input;
    private final Image output;
    private final int start;
    private final int stop;
    private final int THRESHOLD;
    private final double gMax;
    private final float[][] Ix;

    @Override
    protected void compute() {
        if (stop - start > THRESHOLD) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            processing();
        }
    }

    private List<GradientTaskTwo> createSubtasks() {
        List<GradientTaskTwo> subtasks = new ArrayList<>();

        int middle = (start + stop) / 2;

        if (middle > start)
            subtasks.add(GradientTaskTwo.builder()
                    .input(input)
                    .output(output)
                    .start(start)
                    .stop(middle)
                    .THRESHOLD(THRESHOLD)
                    .gMax(gMax)
                    .Ix(Ix)
                    .build());
        if (middle + 1 < stop)
            subtasks.add(GradientTaskTwo.builder()
                    .input(input)
                    .output(output)
                    .start(middle)
                    .stop(stop)
                    .THRESHOLD(THRESHOLD)
                    .gMax(gMax)
                    .Ix(Ix)
                    .build()
            );

        return subtasks;
    }

    private void processing() {
        new SecondGradientFilter().applyFilter(input
                , output
                , start
                , stop
                , (float) gMax
                , Ix);
    }

}
