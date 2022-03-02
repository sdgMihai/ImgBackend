package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class CannyEdgeDetectionFilter extends Filter {
    private int rank;
    private int numtasks;
    private int chunk;
    private List<List<Float>> auxTheta;
    @Value("${NUM_THREADS}")
    Integer NUM_THREADS;

    public CannyEdgeDetectionFilter() {
        this.filter_additional_data = null;
    }
    public CannyEdgeDetectionFilter(FilterAdditionalData filter_additional_data) {
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
