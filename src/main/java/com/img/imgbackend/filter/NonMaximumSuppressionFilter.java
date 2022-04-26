package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NonMaximumSuppressionFilter extends Filter {
    private float[][] theta;

    /**
     * This is the non-maximum suppression filter.
     *
     * @param image    reference to input image.
     * @param newImage reference to output image.
     * @param start    The first line the filter is applied on.
     * @param stop     The line after the last line the filter is applied on.
     */
    @Override
    public void applyFilter(Image image, Image newImage, int start, int stop) {
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float q = 255;
                float r = 255;
                if ((0 <= theta[i][j] && theta[i][j] < 22.5) || (157.5 <= theta[i][j] && theta[i][j] <= 180)) {
                    q = image.matrix[i][j + 1].r;
                    r = image.matrix[i][j - 1].r;
                } else {
                    if ((22.5 <= theta[i][j] && theta[i][j] < 67.5)) {
                        q = image.matrix[i + 1][j - 1].r;
                        r = image.matrix[i - 1][j + 1].r;
                    } else {
                        if ((67.5 <= theta[i][j] && theta[i][j] < 112.5)) {
                            q = image.matrix[i + 1][j].r;
                            r = image.matrix[i - 1][j].r;
                        } else {
                            if ((112.5 <= theta[i][j] && theta[i][j] < 157.5)) {
                                q = image.matrix[i - 1][j - 1].r;
                                r = image.matrix[i + 1][j + 1].r;
                            }
                        }
                    }
                }
                Pixel newPixel = new Pixel();
                newPixel.a = image.matrix[i][j].a;
                if (image.matrix[i][j].r >= q && image.matrix[i][j].r >= r) {
                    newPixel.r = newPixel.g = newPixel.b = image.matrix[i][j].r;
                } else {
                    newPixel.r = newPixel.g = newPixel.b = 0;
                }
                newImage.matrix[i][j] = newPixel;
            }
        }
    }

}
