package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;


public class BrightnessFilter extends AbstractFilter {
    private final float brightness;

    /**
     * constructor
     *
     * @param brightness brightness level, float precision.
     */
    public BrightnessFilter(float brightness) {
        this.brightness = brightness;
    }


    public void applyFilterPh1(Image image, Image newImage, int start, int stop) {
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                newImage.matrix[i][j] = new Pixel(
                        (char) (image.matrix[i][j].r * this.brightness),
                        (char) (image.matrix[i][j].g * this.brightness),
                        (char) (image.matrix[i][j].b * this.brightness),
                        image.matrix[i][j].a);
            }
        }
    }
}
