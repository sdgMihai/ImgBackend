package com.img.imgbackend.filter;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
public class FilterFactory {
    public static Filter filterCreate(String filterName) {
        float param = 0.0f;
        int thetaHeight = 0;
        int thetaWidth = 0;
        return filterCreate(filterName, param, null, thetaHeight, thetaWidth, null);
    }

    /**
     * creeaza un obiect-filtru
     *
     * @param filterName numele filtrului
     * @param param level of brightness or contrast, depending on the @param filterName
     * @return referinta catre obiectul-filtru creat
     */
    public static Filter filterCreate(String filterName, float param,
                                      float[][] theta, int thetaHeight,
                                      int thetaWidth, FilterAdditionalData varargs) {
        Filters it;
        try {
            log.debug(String.format("filter factory creating filter with name [%s]"
                    , filterName.toLowerCase(Locale.ROOT).replace("-", "_")));
            it = Filters.valueOf(filterName.toUpperCase(Locale.ROOT).replace("-", "_"));
        } catch (IllegalArgumentException e) {
            log.debug("Bad filter name exception");
            return new DummyFilter(varargs);
        }

        return switch (it) {
            case SHARPEN -> new SharpenFilter(varargs);
            case EMBOSS -> new EmbossFilter(varargs);
            case BLACK_WHITE -> new BlackWhiteFilter(varargs);
            case BRIGHTNESS -> new BrightnessFilter(param, varargs);
            case CANNY_EDGE_DETECTION -> new CannyEdgeDetectionFilter(varargs);
            case CONTRAST -> new ContrastFilter(param, varargs);
            case DOUBLE_THRESHOLD -> new DoubleThresholdFilter(varargs);
            case EDGE_TRACKING -> new EdgeTrackingFilter(varargs);
            case GAUSSIAN_BLUR -> new GaussianBlurFilter(varargs);
            case NON_MAXIMUM_SUPPRESSION -> new NonMaximumSuppressionFilter(theta, thetaHeight, thetaWidth, varargs);
            case GRADIENT -> new GradientFilter(varargs);
            case SEPIA -> new SepiaFilter(varargs);
        };

    }
}
