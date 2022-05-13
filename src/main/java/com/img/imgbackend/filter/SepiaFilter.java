package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;

public class SepiaFilter extends Filter {

    public SepiaFilter() {
        this.filter_additional_data = null;
    }

    public SepiaFilter(FilterAdditionalData filter_additional_data ) {
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
                int tempColor;

                newPixel.a = image.matrix[i][j].a;
                tempColor = (int)((image.matrix[i][j].r * 0.393) + (image.matrix[i][j].g * 0.769) + (image.matrix[i][j].b * 0.189));
                tempColor = Math.max(tempColor, 0);
                newPixel.r = (char)(Math.min(tempColor, 255));
                tempColor = (int)((image.matrix[i][j].r * 0.349) + (image.matrix[i][j].g * 0.686) + (image.matrix[i][j].b * 0.168));
                tempColor = Math.max(tempColor, 0);
                newPixel.g = (char)(Math.min(tempColor, 255));
                tempColor = (int)((image.matrix[i][j].r * 0.272) + (image.matrix[i][j].g * 0.534) + (image.matrix[i][j].b * 0.131));
                tempColor = Math.max(tempColor, 0);
                newPixel.b = (char)(Math.min(tempColor, 255));


                newImage.matrix[i][j] = newPixel;
            }
        }
    }
}
