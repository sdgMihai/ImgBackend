package com.img.imgbackend.service;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.filter.FilterFactory;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificData;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        ThreadSpecificData threadSpecificData;

        SubImageFilter(ThreadSpecificData threadSpecificData) {
            this.threadSpecificData = threadSpecificData;
        }

        @Override
        public void run() {
            Filter filter;
            for (int i = 0; i < threadSpecificData.getNrFilters(); ++i) {
                String filterName  = threadSpecificData.getFilters().get(i);
                if (threadSpecificData.getThread_id() == 0)
                    System.out.printf("filtrul %s\n", filterName);
                if (filterName.equals("BRIGHTNESS")) {
                    if (threadSpecificData.getThread_id() == 0) {
                        System.out.println(param);
                        param = Double.parseDouble(threadSpecificData.getFilters().get(++i));
                    }
                    try {
                        threadSpecificData.getBarrier().await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    filter = FilterFactory.filterCreate(filterName
                            , (float) param
                            , null
                            , 0
                            , 0
                            , new ThreadSpecificDataT(threadSpecificData.getThread_id()
                                    , threadSpecificData.getBarrier()
                                    , threadSpecificData.getLock()
                                    , threadSpecificData.getNUM_THREADS()));
                } else if (filterName.equals("CONTRAST")) {
                    if (threadSpecificData.getThread_id() == 0) {
                        param = Double.parseDouble(threadSpecificData.getFilters().get(++i));
                        System.out.println(param);
                    }
                    try {
                        threadSpecificData.getBarrier().await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    filter = FilterFactory.filterCreate(filterName
                            , (float) param
                            , null
                            , 0
                            ,0
                            , new ThreadSpecificDataT(threadSpecificData.getThread_id()
                                    , threadSpecificData.getBarrier()
                                    , threadSpecificData.getLock()
                                    , threadSpecificData.getNUM_THREADS()));
                } else {
                    filter = FilterFactory.filterCreate(filterName
                            , 0.0f
                            , null
                            , 0
                            ,0
                            , new ThreadSpecificDataT(threadSpecificData.getThread_id()
                                    , threadSpecificData.getBarrier()
                                    , threadSpecificData.getLock()
                                    , threadSpecificData.getNUM_THREADS()));
                }

                filter.applyFilter(threadSpecificData.getImage(), threadSpecificData.getNewImage());
                System.out.printf("waiting in thread %d at barrier\n", threadSpecificData.getThread_id());
                try {
                    threadSpecificData.getBarrier().await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.printf("passed in thread %d the barrier\n", threadSpecificData.getThread_id());

                if (i == (threadSpecificData.getNrFilters() - 1)) {
                    if (threadSpecificData.getNrFilters() % 2 == 0) {
                        int slice = (threadSpecificData.getImage().height - 2) / NUM_THREADS;
                        int start = Math.max(1, threadSpecificData.getThread_id() * slice);
                        int stop = (threadSpecificData.getThread_id() + 1) * slice;
                        if(threadSpecificData.getThread_id() + 1 == NUM_THREADS) {
                            stop = Math.max((threadSpecificData.getThread_id() + 1) * slice, threadSpecificData.getImage().height - 1);
                        }
                        for (int j = start; j  < stop; ++j) {
                            Pixel[] swp = threadSpecificData.getImage().matrix[i];
                            threadSpecificData.getImage().matrix[i] = threadSpecificData.getNewImage().matrix[i];
                            threadSpecificData.getNewImage().matrix[i] = swp;
                        }
                    }
                    break;
                }

                Image aux = threadSpecificData.getImage();
                threadSpecificData.setImage(threadSpecificData.getNewImage());
                threadSpecificData.setNewImage(aux);
            }
        }
    }


    public Image process(Image image) {
        assert (NUM_THREADS == 4);
        System.out.println("NUM_THREADS" + NUM_THREADS);
        int argc = 1;
        List<String> argv = Arrays.asList("sepia");  // "black-white"
        List<Thread> threads = new ArrayList<>(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);
        Lock lock = new ReentrantLock();

        Image newImage = new Image(image.width - 2, image.height - 2);

        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier, lock, image, newImage, argc, NUM_THREADS, argv));

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
