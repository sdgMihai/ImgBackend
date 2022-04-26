package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.utils.Image;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Slf4j
@AllArgsConstructor
public class SingleOpTask extends RecursiveAction {
    private Image input;
    private Image output;
    private int start;
    private int stop;
    private Filter filter;
    private int THRESHOLD;

    @Override
    protected void compute() {
        if (stop - start > THRESHOLD) {
            ForkJoinTask.invokeAll(createSubtasks());
        } else {
            processing();
        }
    }

    private List<SingleOpTask> createSubtasks() {
        List<SingleOpTask> subtasks = new ArrayList<>();

        int middle = (start + stop) / 2;

        if (middle > start)
            subtasks.add(new SingleOpTask(input, output, start, middle, filter, THRESHOLD));
        if (middle + 1 < stop)
            subtasks.add(new SingleOpTask(input, output, middle, stop, filter, THRESHOLD));

        return subtasks;
    }

    private void processing() {
        filter.applyFilter(input, output, start, stop);
    }
}
