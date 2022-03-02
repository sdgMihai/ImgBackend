package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;

public class DoubleThresholdFilter extends Filter {

    private final float thresholdHigh = 0.06f;
    private final float thresholdLow = 0.05f;
    private float maxVal = -3.40282347e+38F;
    @Value("${NUM_THREADS}")
    private Integer NUM_THREADS;

    public DoubleThresholdFilter() {
        this.filter_additional_data = null;
    }

    public DoubleThresholdFilter(FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
    }
    /**
     * @param image referinta catre imagine
     * @param newImage referinta catre obiectul tip Image
     *          care va contine imaginea rezultata in urma
     *          aplicarii filtrului.
     */
    @Override
    public void applyFilter(Image image, Image newImage) {
        ThreadSpecificDataT tData = (ThreadSpecificDataT) filter_additional_data;
    }
}
