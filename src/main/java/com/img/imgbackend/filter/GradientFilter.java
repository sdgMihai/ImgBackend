package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

public class GradientFilter extends Filter {
    public List<List<Float>> theta; /* place to save theta calculation */
    public int thetaHeight;
    public int thetaWidth;
    private static final float Gx[][] = new float[][]{{-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}};

    private static final float Gy[][] = new float[][]{{1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}};

    private static float gMax = -3.40282347e+38F;
    private static List<List<Float>> Ix, Iy, auxTheta;
    @Value("${NUM_THREADS}")
    private Integer NUM_THREADS;


    public GradientFilter() {
        this.filter_additional_data = null;
    }

    public GradientFilter(FilterAdditionalData filter_additional_data) {
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
