package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;


public class DummyFilter extends Filter {

    public DummyFilter() {
        this.filter_additional_data = null;
    }

    public DummyFilter(FilterAdditionalData filter_additional_data) {
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
            for (int j = 0; j < image.width - 1; ++j) {
                Pixel tmp = image.matrix[i][j];
                newImage.matrix[i][j] = new Pixel(tmp.r,tmp.g,tmp.b,tmp.a);
            }
        }
    }
}
