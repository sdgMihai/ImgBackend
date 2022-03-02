package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import org.springframework.beans.factory.annotation.Value;


public class EdgeTrackingFilter extends Filter {
    @Value("${NUM_THREADS}")
    private Integer NUM_THREADS;

    EdgeTrackingFilter() {
        this.filter_additional_data = null;
    }

    public EdgeTrackingFilter(FilterAdditionalData filter_additional_data) {
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
        //TODO: implementation
    }
}
