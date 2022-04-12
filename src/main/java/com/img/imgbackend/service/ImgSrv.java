package com.img.imgbackend.service;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.filter.FilterFactory;
import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificData;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.mongodb.assertions.Assertions.assertTrue;

@Service
public class ImgSrv {
    double param;
    @Value("${NUM_THREADS}")
    Integer NUM_THREADS;

    public class SubImageFilter implements Runnable {
        ThreadSpecificData data;
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubImageFilter.class);

        public SubImageFilter(ThreadSpecificData threadSpecificData) {
            this.data = threadSpecificData;
        }

        @Override
        public void run() {
            Filter filter;
            if (data.getThread_id() == 0) {
                log.debug(String.format("applying %d filters", data.getNrFilters()));
            }
            for (int i = 0; i < data.getNrFilters(); ++i) {
                String filterName  = data.getFilters().get(i);
                if (data.getThread_id() == 0) {
                    log.debug("filter " + filterName + " executes");
                }
                if (filterName.toLowerCase(Locale.ROOT)
                        .equals(Filters.BRIGHTNESS.toString().toLowerCase(Locale.ROOT))) {
                    // increment index to get the brightness level from data.filters
                    ++i;
                    param = Double.parseDouble(data.getFilters().get(i));
                    log.debug(String.format("using level %f", param));

                    filter = FilterFactory.filterCreate(filterName
                            , (float) param
                            , null
                            , 0
                            , 0
                            , new ThreadSpecificDataT(data.getThread_id()
                                    , data.getBarrier()
                                    , data.getNUM_THREADS()));
                } else if (filterName.toLowerCase(Locale.ROOT)
                        .equals(Filters.CONTRAST.toString().toLowerCase(Locale.ROOT))) {
                        param = Double.parseDouble(data.getFilters().get(++i));
                        System.out.println(param);

                    filter = FilterFactory.filterCreate(filterName
                            , (float) param
                            , null
                            , 0
                            ,0
                            , new ThreadSpecificDataT(data.getThread_id()
                                    , data.getBarrier()
                                    , data.getNUM_THREADS()));
                } else {
                    filter = FilterFactory.filterCreate(filterName
                            , 0.0f
                            , null
                            , 0
                            ,0
                            , new ThreadSpecificDataT(data.getThread_id()
                                    , data.getBarrier()
                                    , data.getNUM_THREADS()));
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
                log.debug("passed in thread " + data.getThread_id() + "the barrier\n");

                // this piece of code isn't actually tested yet
                // TODO: upon testing rm this comment
                if (i == (data.getNrFilters() - 1)) {
                    if (data.getNrFilters() % 2 == 0) {
                        int slice = (data.getImage().height - 2) / NUM_THREADS;
                        int start = Math.max(1, data.getThread_id() * slice);
                        int stop = (data.getThread_id() + 1) * slice;
                        if(data.getThread_id() + 1 == NUM_THREADS) {
                            stop = Math.max((data.getThread_id() + 1) * slice, data.getImage().height - 1);
                        }
                        for (int j = start; j  < stop; ++j) {
                            Pixel[] swp = data.getImage().matrix[i];
                            data.getImage().matrix[i] = data.getNewImage().matrix[i];
                            data.getNewImage().matrix[i] = swp;
                        }
                    }
                    break;
                }

                Image aux = data.getImage();
                data.setImage(data.getNewImage());
                data.setNewImage(aux);
            }
        }
    }


    public Image process(Image image, List<String> filter) {
        assert (NUM_THREADS == 4);
        List<Callable<Object>> threads = new ArrayList<>(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        Image newImage = new Image(image.width - 2, image.height - 2);

        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier, image, newImage, filter.size(), NUM_THREADS, filter));

        for (int i = 0; i < NUM_THREADS; i++) {
            threads.add(
                    Executors.callable(
                            new SubImageFilter(
                                    specificDataList.get(i))));
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

}
