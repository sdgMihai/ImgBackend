package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;


public class BrightnessFilter extends Filter {
    private float brightness;
    @Value("${NUM_THREADS}")
    Integer NUM_THREADS;

    /**
     * constructor
     *
     * @param brightness
     */
    public BrightnessFilter(float brightness) {
        this.brightness = brightness;
        this.filter_additional_data = null;
    }

    /**
     * constructor
     *
     * @param brightness
     */
    public BrightnessFilter(float brightness, FilterAdditionalData filter_additional_data) {
        this.brightness = brightness;
        this.filter_additional_data = filter_additional_data;
    }

    /**
     * @param image    referinta catre imagine
     * @param newImage referinta catre obiectul tip Image
     *                 care va contine imaginea rezultata in urma
     *                 aplicarii filtrului.
     */
    @Override
    public void applyFilter(Image image, Image newImage) {
        ThreadSpecificDataT tData = (ThreadSpecificDataT) filter_additional_data;
    }
}
