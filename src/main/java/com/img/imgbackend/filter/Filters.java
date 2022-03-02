package com.img.imgbackend.filter;

import java.util.HashMap;
import java.util.Map;

public class Filters {
    /**
     * filtrele disponibile - id-urile filtrelor
     */
    public enum FILTER {
        SHARPEN,
        EMBOSS,
        SEPIA,
        CONTRAST,
        BRIGHTNESS,
        BLACK_WHITE,
        GAUSSIAN_BLUR,
        NON_MAXIMUM_SUPPRESSION,
        DOUBLE_TRESHOLD,
        EDGE_TRACKING,
        GRADIENT,
        CANNY_EDGE_DETECTION
    }

    /**
     * asociere intre numele filtrului si id-ul acestuia
     */
    final static Map<String, FILTER> filters = new HashMap<>();

    static {
        filters.put("sharpen", FILTER.SHARPEN);
        filters.put("emboss", FILTER.EMBOSS);
        filters.put("sepia", FILTER.SEPIA);
        filters.put("contrast", FILTER.CONTRAST);
        filters.put("brightness", FILTER.BRIGHTNESS);
        filters.put("black-white", FILTER.BLACK_WHITE);
        filters.put("gaussian-blur", FILTER.GAUSSIAN_BLUR);
        filters.put("non-maximum-suppression", FILTER.NON_MAXIMUM_SUPPRESSION);
        filters.put("double-threshold", FILTER.DOUBLE_TRESHOLD);
        filters.put("edge-tracking", FILTER.EDGE_TRACKING);
        filters.put("gradient", FILTER.GRADIENT);
        filters.put("canny-edge-detection", FILTER.CANNY_EDGE_DETECTION);
    }
}
