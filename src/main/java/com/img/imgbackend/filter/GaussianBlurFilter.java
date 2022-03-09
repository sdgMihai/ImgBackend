package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;

public class GaussianBlurFilter extends Filter {
    static final float kernel[][] = new float[][]{{1.f / 16.f, 2.f / 16.f, 1.f / 16.f},
            {2.f / 16.f, 4.f / 16.f, 2.f / 16.f},
            {1.f / 16.f, 2.f / 16.f, 1.f / 16.f}};

    GaussianBlurFilter() {
        this.filter_additional_data = null;
    }

    public GaussianBlurFilter(FilterAdditionalData filter_additional_data) {
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
        int slice = (image.height - 2) / tData.NUM_THREADS;//imaginea va avea un rand de pixeli deasupra si unul dedesubt
        //de aici '-2' din ecuatie
        int start = Math.max(1, tData.threadID * slice);
        int stop = (tData.threadID + 1) * slice;
        if (tData.threadID + 1 == tData.NUM_THREADS) {
            stop = Math.max((tData.threadID + 1) * slice, image.height - 1);
        }

        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                Pixel newPixel = new Pixel();
                float red, green, blue;
                red = green = blue = 0;
                newPixel.a = image.matrix[i][j].a;
                newPixel.r = newPixel.b = newPixel.g = 0;

                for (int ki = -1; ki <= 1; ++ki) {
                    for (int kj = -1; kj <= 1; ++kj) {
                        red += (float) (image.matrix[i + ki][j + kj].r) * kernel[ki + 1][kj + 1];
                        green += (float) (image.matrix[i + ki][j + kj].g) * kernel[ki + 1][kj + 1];
                        blue += (float) (image.matrix[i + ki][j + kj].b) * kernel[ki + 1][kj + 1];
                    }
                }

                red = (red < 0) ? 0 : red;
                green = (green < 0) ? 0 : green;
                blue = (blue < 0) ? 0 : blue;
                newPixel.r = (char) ((red > 255) ? 255 : red);
                newPixel.g = (char) ((green > 255) ? 255 : green);
                newPixel.b = (char) ((blue > 255) ? 255 : blue);
                newImage.matrix[i][j] = newPixel;
            }
        }
    }
}
