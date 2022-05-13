package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;

public class EmbossFilter extends Filter {
    static final float[][] kernel = new float[][]{{0, 1, 0},
        {0, 0, 0},
        {0, -1, 0}};

    EmbossFilter() {
        this.filter_additional_data = null;
    }

    public EmbossFilter(FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
    }

    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start    first line to be processed from input image.
     * @param stop     past last line to be processed from input image.
     */
    @Override
    public void applyFilter(Image image, Image newImage, int start, int stop){
        ThreadSpecificDataT tData = (ThreadSpecificDataT) filter_additional_data;

        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                Pixel newPixel = new Pixel();
                float red, green, blue;
                red = green = blue = 0;
                newPixel.a = image.matrix[i][j].a;

                for (int ki = -1; ki <= 1; ++ki) {
                    for (int kj = -1; kj <= 1; ++kj) {
                        red   += (float)(image.matrix[i + ki][j + kj].r) * kernel[ki + 1][kj + 1];
                        green += (float)(image.matrix[i + ki][j + kj].g) * kernel[ki + 1][kj + 1];
                        blue  += (float)(image.matrix[i + ki][j + kj].b) * kernel[ki + 1][kj + 1];
                    }
                }

                red = (red < 0) ? 0 : red;
                green = (green < 0) ? 0 : green;
                blue = (blue < 0) ? 0 : blue;
                newPixel.r = (char)((red > 255) ? 255 : red);
                newPixel.g = (char)((green > 255) ? 255 : green);
                newPixel.b = (char)((blue > 255) ? 255 : blue);
                newImage.matrix[i][j] = newPixel;
            }
        }
    }
}
