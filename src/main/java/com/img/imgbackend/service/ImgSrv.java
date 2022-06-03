package com.img.imgbackend.service;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.utils.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.*;

import static com.mongodb.assertions.Assertions.assertTrue;

@Service
public class ImgSrv {
    @Value("${NUM_THREADS}")
    Integer NUM_THREADS;

    public Image process(Image image, String[] filterNames, String[] filterParams) {
        assert (NUM_THREADS == 4);
        List<Callable<Object>> threads = new ArrayList<>(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);
        GradientData gradientData = new GradientData(image.height, image.width, NUM_THREADS);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        Image newImage = new Image(image.width - 2, image.height - 2);

        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier, image, newImage, filterNames.length, NUM_THREADS, filterNames, gradientData));

        for (int i = 0; i < NUM_THREADS; i++) {
            threads.add(
                    Executors.callable(
                            new SubImageFilter(
                                    FilterService.getFilters(filterNames
                                            , filterParams
                                            , specificDataList.get(i))
                                    , specificDataList.get(i))));
        }

        try {
            executor.invokeAll(threads);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        try {
            assertTrue(executor.awaitTermination(40, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return newImage;
    }

    public static class SubImageFilter implements Runnable {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubImageFilter.class);
        List<Filter> filters;
        ThreadSpecificData data;
        int start;
        int stop;

        public SubImageFilter(List<Filter> filters, ThreadSpecificData data) {
            this.filters = filters;
            this.data = data;
        }

        @Override
        public void run() {
            int slice = (data.getImage().height - 2) / data.getNUM_THREADS();
            start = Math.max(1, data.getThread_id() * slice);
            stop = (data.getThread_id() + 1) * slice;
            if (data.getThread_id() + 1 == data.getNUM_THREADS()) {
                stop = Math.max((data.getThread_id() + 1) * slice, data.getImage().height - 1);
            }

            ListIterator<Filter> filterIt = filters.listIterator();
            while (filterIt.hasNext()) {
                int i = filterIt.nextIndex();
                Filter filter = filterIt.next();
                if (((ThreadSpecificDataT) filter.filter_additional_data).threadID == 0) {
                    log.debug(String.format("applying %d filters", filters.size()));
                }

                try {
                    filter.applyFilter(data.getImage(), data.getNewImage());
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    data.getBarrier().await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                log.debug(i + "pass in thread no " + data.getThread_id() + " the barrier\n");
                if (filterIt.hasNext()) {
                    ImageUtils.swap(data.getImage(), data.getNewImage(), start, stop);
                }
            }
        }
    }
}
