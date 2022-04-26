package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;


public class ContrastFilter extends Filter {
    private final float contrast;

    /**
     * constructor
     *
     * @param contrast float value [-128 , 128]
     */
    public ContrastFilter(float contrast) {
        this.contrast = contrast;
    }

    /**
     * @param image    input image reference
     * @param newImage output image reference
     */
    @Override
    public void applyFilter(Image image, Image newImage, int start, int stop) {
        float factor = (float)  (259 * (this.contrast + 255.) / (255. * (259. - this.contrast)));
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                Pixel newPixel = new Pixel();
                float tempColor;

                newPixel.a = image.matrix[i][j].a;
                tempColor = factor * (image.matrix[i][j].r - 128) + 128;
                tempColor = (tempColor < 0) ? 0 : tempColor;
                newPixel.r = (char) ((tempColor > 255) ? 255 : tempColor);
                tempColor = factor * (image.matrix[i][j].g - 128) + 128;
                tempColor = (tempColor < 0) ? 0 : tempColor;
                newPixel.g = (char) ((tempColor > 255) ? 255 : tempColor);
                tempColor = factor * (image.matrix[i][j].b - 128) + 128;
                tempColor = (tempColor < 0) ? 0 : tempColor;
                newPixel.b = (char) ((tempColor > 255) ? 255 : tempColor);

                newImage.matrix[i][j] = newPixel;
            }
        }
    }
}
