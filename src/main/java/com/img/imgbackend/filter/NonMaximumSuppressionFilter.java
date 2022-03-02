package com.img.imgbackend.filter;



import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class NonMaximumSuppressionFilter extends Filter {
    private List<List<Float>> theta;
    private final int thetaHeight;
    private final int thetaWidth;
    @Value("${NUM_THREADS}")
    private Integer NUM_THREADS;

    public NonMaximumSuppressionFilter(List<List<Float>> theta, int thetaHeight,
                                       int thetaWidth) {
        this.filter_additional_data = null;
        this.thetaWidth = thetaWidth;
        this.thetaHeight = thetaHeight;
        this.theta = theta;
    }

    public NonMaximumSuppressionFilter(List<List<Float>> theta, int thetaHeight,
                                       int thetaWidth, FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
        this.thetaWidth = thetaWidth;
        this.thetaHeight = thetaHeight;
        this.theta = theta;
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
