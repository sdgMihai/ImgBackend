package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;


public class BrightnessFilter extends Filter {
    private float brightness;

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
        int slice = (image.height - 2) / tData.NUM_THREADS;  // imaginea va avea un rand de pixeli deasupra si unul dedesubt
        //de aici '-2' din ecuatie
        int start = Math.max(1, tData.threadID * slice);
        int stop = (tData.threadID + 1) * slice;
        if (tData.threadID + 1 == tData.NUM_THREADS) {
            stop = Math.max((tData.threadID + 1) * slice, image.height - 1);
        }

        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                newImage.matrix[i][j] =  new Pixel(
                        (char) (image.matrix[i][j].r * this.brightness),
                        (char) (image.matrix[i][j].g * this.brightness),
                        (char) (image.matrix[i][j].b * this.brightness),
                        image.matrix[i][j].a);
            }
        }
    }
}
