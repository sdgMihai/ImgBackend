package com.img.imgbackend.service;

import com.img.imgbackend.filter.tasks.TaskFactory;
import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Service
@Slf4j
public class ImgSrv {
    @Value("${NUM_THREADS}")
    private Integer NUM_THREADS;

    private final ForkJoinPool commonPool = ForkJoinPool.commonPool();
    private static int THRESHOLD;

    @Value("${THRESHOLD}")
    public void setTHRESHOLD(int THRESHOLD) {
        ImgSrv.THRESHOLD = THRESHOLD;
    }

    public Image process(Image image, String[] filterName, String[] filterParams) {
        assert (NUM_THREADS == 4);
        Image newImage = new Image(image.width - 2, image.height - 2);
        List<RecursiveAction> filters = TaskFactory.getFilters(filterName, filterParams, image, newImage, THRESHOLD);
        filters.forEach(
                commonPool::invoke
        );

        return newImage;
    }

}
