package com.img.imgbackend.filter;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public class FilterFactory {
    public static Filter filterCreate(String filterName) {
        float param = 0.0f;
        return filterCreate(filterName, param, null);
    }

    /**
     * Creates a filter object.
     *
     * @param filterName the filter's name
     * @param param level of brightness or contrast, depending on the @param filterName
     * @return the created instance.
     */
    public static Filter filterCreate(String filterName, float param,
                                      float[][] theta) {
        Filters it;
        try {
            log.debug(String.format("filter factory creating filter with name [%s]"
                    , filterName.toLowerCase(Locale.ROOT).replace("-", "_")));
            it = Filters.valueOf(filterName.toUpperCase(Locale.ROOT).replace("-", "_"));
        } catch (IllegalArgumentException e) {
            log.debug("Bad filter name exception");
            return new DummyFilter();
        }

        return switch (it) {
            case SHARPEN -> new SharpenFilter();
            case EMBOSS -> new EmbossFilter();
            case BLACK_WHITE -> new BlackWhiteFilter();
            case BRIGHTNESS -> new BrightnessFilter(param);
            case CONTRAST -> new ContrastFilter(param);
            case EDGE_TRACKING -> new EdgeTrackingFilter();
            case GAUSSIAN_BLUR -> new GaussianBlurFilter();
            case NON_MAXIMUM_SUPPRESSION -> new NonMaximumSuppressionFilter(theta);
            case SEPIA -> new SepiaFilter();
            default -> throw new IllegalStateException("Unexpected value: " + it);
        };

    }
}
