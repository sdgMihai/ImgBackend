package com.img.imgbackend.filter;

public enum Filters {
    /**
     * filtrele disponibile - id-urile filtrelor
     */

        SHARPEN("sharpen"),
        EMBOSS("emboss"),
        SEPIA("sepia"),
        CONTRAST("contrast"),
        BRIGHTNESS("brightness"),
        BLACK_WHITE("black-white"),
        GAUSSIAN_BLUR("gaussian-blur"),
        NON_MAXIMUM_SUPPRESSION("non-maximum-suppression"),
        DOUBLE_THRESHOLD("double-threshold"),
        EDGE_TRACKING("edge-tracking"),
        GRADIENT("gradient"),
        CANNY_EDGE_DETECTION("canny-edge-detection");

    /**
     * association btw filter name and its id
     * bc. dash can't be found in enum fields, the default toString() method can't be used, but rather a String field
     */
    private final String value;

    Filters(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
