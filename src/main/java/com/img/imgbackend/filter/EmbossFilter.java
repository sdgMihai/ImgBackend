package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;

public class EmbossFilter extends Filter {
    static final float[][] kernel = new float[][]{{0, 1, 0},
        {0, 0, 0},
        {0, -1, 0}};
    @Value("${NUM_THREADS}")
    private Integer NUM_THREADS;

    EmbossFilter() {
        this.filter_additional_data = null;
    }

    public EmbossFilter(FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
    }

    /**
     * @param image    referinta catre imagine
     * @param newImage referinta catre obiectul tip Image
     *                 care va contine imaginea rezultata in urma
     *                 aplicarii filtrului.
     */
    @Override
    public void applyFilter(Image image, Image newImage){
        ThreadSpecificDataT tData = (ThreadSpecificDataT) filter_additional_data;
    }
}
