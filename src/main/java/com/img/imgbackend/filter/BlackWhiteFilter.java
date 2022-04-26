package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlackWhiteFilter extends Filter {

    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start    first line to be processed from input image.
     * @param stop     past last line to be processed from input image.
     */
    @Override
    public void applyFilter(Image image, Image newImage, int start, int stop) {
        log.debug("applying bw filter");
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                int gray = (int) (0.2126 * image.matrix[i][j].r +
                        0.7152 * image.matrix[i][j].g +
                        0.0722 * image.matrix[i][j].b);
                gray = Math.min(gray, 255);
                newImage.matrix[i][j] = new Pixel((char) gray, (char) gray, (char) gray, image.matrix[i][j].a);
            }
        }
    }

}
