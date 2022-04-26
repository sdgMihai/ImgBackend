package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;


public class EdgeTrackingFilter extends Filter {
    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start first line to be processed from input image.
     * @param stop past last line to be processed from input image.
     */
    @Override
    public void applyFilter(Image image, Image newImage, int start, int stop) {
        final int weak = 100;
        final int strong = 255;

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
