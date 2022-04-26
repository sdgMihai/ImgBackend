package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.filter.*;
import com.img.imgbackend.utils.Image;
import lombok.AllArgsConstructor;

import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
public class CannyEdgeDetectionTask  extends RecursiveAction {
    private Image input;
    private Image output;
    private int start;
    private int stop;
    private int THRESHOLD;

    @Override
    protected void compute() {

        final SingleOpTask bwTask = new SingleOpTask(input, output, start, stop, new BlackWhiteFilter(), THRESHOLD);
        bwTask.fork();
        bwTask.join();

        final SingleOpTask gbTask = new SingleOpTask(output, input, start, stop, new GaussianBlurFilter(), THRESHOLD);
        gbTask.fork();
        gbTask.join();

        final GradientTask gTask = new GradientTask(input, output, start, stop, THRESHOLD);
        gTask.fork();
        final float[][] theta = gTask.join();

        final SingleOpTask nmsTask = new SingleOpTask(output
                , input
                , start
                , stop
                , new NonMaximumSuppressionFilter(theta)
                , THRESHOLD);
        nmsTask.fork();
        nmsTask.join();

        final DoubleThresholdTask dtTask = new DoubleThresholdTask(input, output, start, stop, THRESHOLD);
        dtTask.fork();
        dtTask.join();

        final SingleOpTask etTask = new SingleOpTask(output
                , input
                , start
                , stop
                , new EdgeTrackingFilter()
                , THRESHOLD);
        etTask.fork();
        etTask.join();

        final SingleOpTask cleanNoiseTask = new SingleOpTask(input
                , output
                , start
                , stop
                , new FirstCannyEdgeDetectionFilter()
                , THRESHOLD);
        cleanNoiseTask.fork();
        cleanNoiseTask.join();

    }
}
