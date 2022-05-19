package com.img.imgbackend.service;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ImageUtils;
import com.img.imgbackend.utils.ThreadSpecificData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Service
public class ImgSrv {
    public static final BiConsumer<List<Filter>, ThreadSpecificData> applyFilter = (List<Filter> filters, ThreadSpecificData data) -> {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImgSrv.class);
        ListIterator<Filter> filterIt = filters.listIterator();
        while (filterIt.hasNext()) {
            Filter filter = filterIt.next();
            log.debug("applying " + filter.toString() + "filter");
            filter.applyFilter(data.getImage(), data.getNewImage(), data.getPARALLELISM());

            if (filterIt.hasNext()) {
                ImageUtils.swap(data.getImage(), data.getNewImage(), data.getPARALLELISM());
            }
        }
    };
    @Value("${NUM_THREADS}")
    Integer PARALLELISM;

    @Async
    public CompletableFuture<Image> process(Image image, String[] filterNames, String[] filterParams) {
        return CompletableFuture.supplyAsync(() -> {
            assert (PARALLELISM == 4);
            Image newImage = new Image(image.width - 2, image.height - 2);
            final List<Filter> filters = FilterService.getFilters(filterNames, filterParams);

            ImgSrv.applyFilter
                    .accept(filters, new ThreadSpecificData(PARALLELISM, image, newImage));
            return newImage;
        });
    }

}
