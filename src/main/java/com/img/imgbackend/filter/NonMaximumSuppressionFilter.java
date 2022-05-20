package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ImageUtils;
import com.img.imgbackend.utils.Pixel;
import org.springframework.data.util.Pair;

import java.util.concurrent.CompletableFuture;

public class NonMaximumSuppressionFilter implements Filter {
    private final float[][] theta;
    private final int thetaHeight;
    private final int thetaWidth;

    public NonMaximumSuppressionFilter(float[][] theta, int thetaHeight, int thetaWidth) {
        this.thetaWidth = thetaWidth;
        this.thetaHeight = thetaHeight;
        this.theta = theta;
    }

    /**
     * @param in          input image reference.
     * @param out         output image reference.
     * @param PARALLELISM integer value denoting the number of task running in parallel.
     */
    @Override
    public void applyFilter(Image in, Image out, final int PARALLELISM) {
        CompletableFuture<Void>[] partialFilters = new CompletableFuture[PARALLELISM];
        Pair<Integer, Integer>[] ranges = ImageUtils.getRange(PARALLELISM, in.height);
        for (int i = 0; i < PARALLELISM; i++) {
            int start = ranges[i].getFirst();
            int stop = ranges[i].getSecond();
            partialFilters[i] = CompletableFuture.runAsync(() -> applyFilterPh1(in, out, start, stop));
        }
        CompletableFuture.allOf(partialFilters).join();
    }


    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start    first line to be processed from input image.
     * @param stop     past last line to be processed from input image.
     */
    public void applyFilterPh1(Image image, Image newImage, int start, int stop) {
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
