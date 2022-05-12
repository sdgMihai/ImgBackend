package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;


public class ContrastFilter extends Filter {
    private final float contrast;

    /**
     * constructor
     *
     * @param contrast a value x describing the contrast level, where x in [-128, 128]
     */
    public ContrastFilter(float contrast, FilterAdditionalData filter_additional_data) {
        this.contrast = contrast;
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

        float factor = (float)  (259. * (this.contrast + 255.) / (255. * (259. - this.contrast)));
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
