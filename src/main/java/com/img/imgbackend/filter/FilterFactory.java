package com.img.imgbackend.filter;

import java.util.List;

public class FilterFactory {
    public static Filter filterCreate(String filterName) {
        float param = 0.0f;
        List<List<Float>> theta = null;
        int thetaHeight = 0;
        int thetaWidth = 0;
        FilterAdditionalData varargs = null;
        return filterCreate(filterName, param, theta, thetaHeight, thetaWidth, varargs);
    }

    /**
     * creeaza un obiect-filtru
     *
     * @param filterName numele filtrului
     * @param param
     * @return referinta catre obiectul-filtru creat
     */
    public static Filter filterCreate(String filterName, float param,
                                      List<List<Float>> theta, int thetaHeight,
                                      int thetaWidth, FilterAdditionalData varargs) {
        Filters.FILTER it = Filters.filters.get(filterName);

        if (it == null) {
            return new DummyFilter();
        }

        switch (it) {
            case SHARPEN:
                return new SharpenFilter(varargs);
            case EMBOSS:
                return new EmbossFilter(varargs);
            case BLACK_WHITE:
                return new BlackWhiteFilter(varargs);
            case BRIGHTNESS:
                return new BrightnessFilter(param, varargs);
            case CANNY_EDGE_DETECTION:
                return new CannyEdgeDetectionFilter(varargs);
            case CONTRAST:
                return new ContrastFilter(param, varargs);
            case DOUBLE_TRESHOLD:
                return new DoubleThresholdFilter(varargs);
            case EDGE_TRACKING:
                return new EdgeTrackingFilter(varargs);
            case GAUSSIAN_BLUR:
                return new GaussianBlurFilter(varargs);
            case NON_MAXIMUM_SUPPRESSION:
                return new NonMaximumSuppressionFilter(theta, thetaHeight, thetaWidth, varargs);
            case GRADIENT:
                return new GradientFilter(varargs);
            case SEPIA:
                return new SepiaFilter(varargs);
        }

        return new DummyFilter(varargs);
    }
}
