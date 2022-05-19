package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;


public class DummyFilter extends AbstractFilter {
    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start    first line to be processed from input image.
     * @param stop     past last line to be processed from input image.
     */
    public void applyFilterPh1(Image image, Image newImage, int start, int stop){
        for (int i = start; i < stop; ++i) {
            for (int j = 0; j < image.width - 1; ++j) {
                Pixel tmp = image.matrix[i][j];
                newImage.matrix[i][j] = new Pixel(tmp.r,tmp.g,tmp.b,tmp.a);
            }
        }
    }
}
