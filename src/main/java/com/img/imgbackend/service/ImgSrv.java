package com.img.imgbackend.service;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.filter.FilterFactory;
import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.utils.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ImgSrv {
    double param;
    @Value("${NUM_THREADS}")
    Integer NUM_THREADS;

    class SubImageFilter extends Thread {
        ThreadSpecificData data;
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubImageFilter.class);

        SubImageFilter(ThreadSpecificData threadSpecificData) {
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
                                    , data.getLock()
                                    , data.getNUM_THREADS()));
                } else if (filterName.toLowerCase(Locale.ROOT)
                        .equals(Filters.CONTRAST.toString().toLowerCase(Locale.ROOT))) {

                        param = Double.parseDouble(data.getFilters().get(++i));
                        log.debug(String.format("using level %f", param));

                        filter = FilterFactory.filterCreate(filterName
                                , (float) param
                                , null
                                , 0
                                ,0
                                , new ThreadSpecificDataT(data.getThread_id()
                                        , data.getBarrier()
                                        , data.getLock()
                                        , data.getNUM_THREADS()));
                } else {
                    filter = FilterFactory.filterCreate(filterName
                            , 0.0f
                            , null
                            , 0
                            ,0
                            , new ThreadSpecificDataT(data.getThread_id()
                                    , data.getBarrier()
                                    , data.getLock()
                                    , data.getNUM_THREADS()));
                }

                try {
                    filter.applyFilter(data.getImage(), data.getNewImage());
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }

                data.getBarrier().await();

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
        List<Thread> threads = new ArrayList<>(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        Barrier barrier = new Barrier(NUM_THREADS);
        Object lock = new Object();

        Image newImage = new Image(image.width - 2, image.height - 2);

        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier, lock, image, newImage, filter.size(), NUM_THREADS, filter));

        for (int i = 0; i < NUM_THREADS; i++) {
            threads.add(new SubImageFilter(specificDataList.get(i)));
            threads.get(i).start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return newImage;
    }

}
