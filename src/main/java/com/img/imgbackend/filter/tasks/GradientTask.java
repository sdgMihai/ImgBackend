package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.utils.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.concurrent.RecursiveTask;

@Builder
@AllArgsConstructor
public class GradientTask extends RecursiveTask<float[][]> {
    private final Image input;
    private final Image output;
    private final int start;
    private final int stop;
    private final int THRESHOLD;

    @Override
    protected float[][] compute() {
        float[][] theta = new float[input.height][];
        float[][] ix = new float[input.height][];
        float[][] iy = new float[input.height][];

        for (int i = 0; i < input.height; ++i) {
            ix[i] = new float[input.width];
            iy[i] = new float[input.width];
            theta[i] = new float[input.width];
        }

        GradientTaskOne taskOne = GradientTaskOne.builder()
                .input(input)
                .start(start)
                .stop(stop)
                .THRESHOLD(THRESHOLD)
                .theta(theta)
                .ix(ix)
                .iy(iy)
                .build();

        taskOne.fork();
        final double gMax = taskOne.join();

        GradientTaskTwo taskTwo = GradientTaskTwo.builder()
                .input(input)
                .output(output)
                .start(start)
                .stop(stop)
                .THRESHOLD(THRESHOLD)
                .gMax(gMax)
                .Ix(ix)
                .build();

        taskTwo.fork();
        taskTwo.join();
        return theta;
    }
}
