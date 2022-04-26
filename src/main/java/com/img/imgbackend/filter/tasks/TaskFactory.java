package com.img.imgbackend.filter.tasks;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.filter.FilterFactory;
import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.RecursiveAction;

@Slf4j
public class TaskFactory {
    private static int THRESHOLD;

    public static boolean filterNameEquals(String filter1, String filter2) {
        return filter1.toLowerCase(Locale.ROOT)
                .equals(filter2.toLowerCase(Locale.ROOT));
    }

    public static List<RecursiveAction> getFilters(String[] filterNames
            , String[] filterParams
            , Image in
            , Image out
            , int THRESHOLD) {
        TaskFactory.THRESHOLD = THRESHOLD;
        Iterator<String> filterParamsIt = null;
        if (filterParams != null) {
            filterParamsIt = Arrays.stream(filterParams).iterator();
        }

        List<RecursiveAction> filters = new ArrayList<>(filterNames.length);
        for (var filterName : filterNames) {
            if (filterNameEquals(filterName, Filters.CANNY_EDGE_DETECTION.toString())
            || filterNameEquals(filterName, Filters.DOUBLE_THRESHOLD.toString())) {

                filters.add(getFilterTask(filterName
                        , in
                        , out
                        , 1
                        , in.height - 1
                        , null
                ));
            } else if (filterNameEquals(filterName, Filters.CONTRAST.toString())
                    || filterNameEquals(filterName, Filters.BRIGHTNESS.toString())) {
                double param = Double.parseDouble(filterParamsIt.next());
                log.debug(String.format("using level %f", param));

                filters.add(getFilterTask(filterName
                        , in
                        , out
                        , 1
                        , in.height - 1
                        , FilterFactory.filterCreate(filterName
                                , (float) param
                                , null
                                )
                ));
            } else {
                log.debug(String.format("creating filter with THRESHOLD %d", THRESHOLD));
                filters.add(getFilterTask(filterName
                        , in
                        , out
                        , 1
                        , in.height - 1
                        , FilterFactory.filterCreate(filterName
                                , 0.0f
                                , null)
                ));
            }
        }
        return filters;
    }

    private static RecursiveAction getFilterTask(String filterName
            , final Image input
            , final Image output
            , final int start
            , final int stop
            , final Filter filter) {
        Filters it = Filters.valueOf(filterName.toUpperCase(Locale.ROOT)
                .replace("-", "_"));
        return switch (it) {
            case SHARPEN, EMBOSS, BLACK_WHITE, BRIGHTNESS, CONTRAST, EDGE_TRACKING, GAUSSIAN_BLUR, SEPIA, NON_MAXIMUM_SUPPRESSION
                    -> new SingleOpTask(input
                    , output
                    , start
                    , stop
                    , filter
                    , THRESHOLD);
            case CANNY_EDGE_DETECTION -> new CannyEdgeDetectionTask(input, output, start, stop, THRESHOLD);
            case DOUBLE_THRESHOLD -> new DoubleThresholdTask(input, output, start, stop, THRESHOLD);
        };
    }
}
