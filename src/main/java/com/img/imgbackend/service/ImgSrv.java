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

@Service
public class ImgSrv {
    double param;
    @Value("${NUM_THREADS}")
    Integer NUM_THREADS;

    public Image process(Image image, List<String> filter) {
        assert (NUM_THREADS == 4);
        List<Thread> threads = new ArrayList<>(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        Barrier barrier = new Barrier(NUM_THREADS);
        Object lock = new Object();
        DataInit dataInit = new DataInit();

        Image newImage = new Image(image.width - 2, image.height - 2);

        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier, lock, image, newImage, filter.size(), NUM_THREADS, filter, dataInit));

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

    public class SubImageFilter extends Thread {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubImageFilter.class);
        ThreadSpecificData data;

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
                String filterName = data.getFilters().get(i);
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
                                    , data.getNUM_THREADS()
                                    , data.getDataInit()));
                } else if (filterName.toLowerCase(Locale.ROOT)
                        .equals(Filters.CONTRAST.toString().toLowerCase(Locale.ROOT))) {

                    param = Double.parseDouble(data.getFilters().get(++i));
                    log.debug(String.format("using level %f", param));

                    filter = FilterFactory.filterCreate(filterName
                            , (float) param
                            , null
                            , 0
                            , 0
                            , new ThreadSpecificDataT(data.getThread_id()
                                    , data.getBarrier()
                                    , data.getLock()
                                    , data.getNUM_THREADS()
                                    , data.getDataInit()));
                } else {
                    filter = FilterFactory.filterCreate(filterName
                            , 0.0f
                            , null
                            , 0
                            , 0
                            , new ThreadSpecificDataT(data.getThread_id()
                                    , data.getBarrier()
                                    , data.getLock()
                                    , data.getNUM_THREADS()
                                    , data.getDataInit()));
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

                Pixel[][] aux = data.getImage().matrix;
                data.getImage().matrix = data.getNewImage().matrix;
                data.getNewImage().matrix = aux;
            }
        }
    }

}
