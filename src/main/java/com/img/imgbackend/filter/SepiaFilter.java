package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;

public class SepiaFilter extends AbstractFilter {

    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start    first line to be processed from input image.
     * @param stop     past last line to be processed from input image.
     */

    public void applyFilterPh1(Image image, Image newImage, int start, int stop) {
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                Pixel newPixel = new Pixel();
                int tempColor;

                newPixel.a = image.matrix[i][j].a;
                tempColor = (int) ((image.matrix[i][j].r * 0.393) + (image.matrix[i][j].g * 0.769) + (image.matrix[i][j].b * 0.189));
                tempColor = Math.max(tempColor, 0);
                newPixel.r = (char) (Math.min(tempColor, 255));
                tempColor = (int) ((image.matrix[i][j].r * 0.349) + (image.matrix[i][j].g * 0.686) + (image.matrix[i][j].b * 0.168));
                tempColor = Math.max(tempColor, 0);
                newPixel.g = (char) (Math.min(tempColor, 255));
                tempColor = (int) ((image.matrix[i][j].r * 0.272) + (image.matrix[i][j].g * 0.534) + (image.matrix[i][j].b * 0.131));
                tempColor = Math.max(tempColor, 0);
                newPixel.b = (char) (Math.min(tempColor, 255));


                newImage.matrix[i][j] = newPixel;
            }
        }
    }
}
