package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirstThresholdFilter {
    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start    first line to be processed from input image.
     * @param stop     past last line to be processed from input image.
     */
    public static Double applyFilter(Image image, Image newImage, int start, int stop) {
        float threadMaxVal = -3.40282347e+38F;
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                threadMaxVal = (threadMaxVal < image.matrix[i][j].r) ? image.matrix[i][j].r : threadMaxVal;
            }
        }
        return (double)threadMaxVal;
    }
}
