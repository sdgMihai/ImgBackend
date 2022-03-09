package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.springframework.beans.factory.annotation.Value;


public class EdgeTrackingFilter extends Filter {

    EdgeTrackingFilter() {
        this.filter_additional_data = null;
    }

    public EdgeTrackingFilter(FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
    }
    /**
     * @param image referinta catre imagine
     * @param newImage referinta catre obiectul tip Image
     *          care va contine imaginea rezultata in urma
     *          aplicarii filtrului.
     */
    @Override
    public void applyFilter(Image image, Image newImage) {
        final int weak = 100;
        final int strong = 255;
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
                if (image.matrix[i][j].r == weak) {
                    if (image.matrix[i - 1][j - 1].r == strong || image.matrix[i - 1][j].r == strong ||
                            image.matrix[i - 1][j + 1].r  == strong || image.matrix[i][j - 1].r == strong ||
                            image.matrix[i][j + 1].r == strong || image.matrix[i + 1][j - 1].r == strong ||
                            image.matrix[i + 1][j].r == strong || image.matrix[i + 1][j + 1].r == strong) {

                        newImage.matrix[i][j] = new Pixel((char) strong, (char) strong, (char) strong, image.matrix[i][j].a);
                    } else {
                        newImage.matrix[i][j] = new Pixel((char) 0, (char) 0, (char) 0, image.matrix[i][j].a);
                    }
                } else {
                    Pixel tmp = image.matrix[i][j];
                    newImage.matrix[i][j] = new Pixel(tmp.r, tmp.g, tmp.b, tmp.a);
                }
            }
        }
    }
}
