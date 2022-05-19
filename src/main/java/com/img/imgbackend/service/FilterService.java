package com.img.imgbackend.service;

import com.img.imgbackend.filter.Filter;
import com.img.imgbackend.filter.FilterFactory;
import com.img.imgbackend.filter.Filters;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class FilterService {

    public static boolean filterNameEquals(String filter1, String filter2) {
        return filter1.toLowerCase(Locale.ROOT)
                .equals(filter2.toLowerCase(Locale.ROOT));
    }

    /**
     Transform filter names and parameters to list of filters.
     */
    public static List<Filter> getFilters(String[] filterNames
            , String[] filterParams) {
        Iterator<String> filterParamsIt = null;
        if (filterParams != null) {
            filterParamsIt = Arrays.stream(filterParams).iterator();
        }

        List<Filter> filters = new ArrayList<>(filterNames.length);
        for (var filterName : filterNames) {
            if (filterNameEquals(filterName, Filters.BRIGHTNESS.toString())) {
                double param = 0;
                assert filterParamsIt != null;
                if (filterParamsIt.hasNext()) {  // increment index to get the brightness level from data.filters
                    param = Double.parseDouble(filterParamsIt.next());
                }
                log.debug(String.format("using level %f", param));

                filters.add(FilterFactory.filterCreate(filterName
                        , (float) param
                        , null
                        , 0
                        , 0));
            } else if (filterNameEquals(filterName, Filters.CONTRAST.toString())) {
                double param = 0;
                assert filterParamsIt != null;
                if (filterParamsIt.hasNext()) {
                    param = Double.parseDouble(filterParamsIt.next());
                }
                log.debug(String.valueOf(param));

                filters.add(FilterFactory.filterCreate(filterName
                        , (float) param
                        , null
                        , 0
                        ,0));
            } else {
                filters.add(FilterFactory.filterCreate(filterName));
            }
        }
        return filters;
    }
}
