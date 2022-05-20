package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ImageUtils;
import com.img.imgbackend.utils.Pixel;
import org.springframework.data.util.Pair;

import java.util.concurrent.CompletableFuture;

public class CannyEdgeDetectionFilter implements Filter {

    /**
     * @param image       input image reference.
     * @param newImage    output image reference.
     * @param PARALLELISM the async futures that can run in parallel
     */
    @Override
    public void applyFilter(Image image, Image newImage, int PARALLELISM) {

        BlackWhiteFilter step1 = new BlackWhiteFilter();
        step1.applyFilter(image, newImage, PARALLELISM);

        GaussianBlurFilter step2 = new GaussianBlurFilter();
        step2.applyFilter(newImage, image, PARALLELISM);
        GradientFilter step3 = new GradientFilter();
        step3.applyFilter(image, newImage, 1);
        float[][] auxTheta = step3.theta;

        NonMaximumSuppressionFilter step4 = new NonMaximumSuppressionFilter(auxTheta, step3.thetaHeight, step3.thetaWidth);
        step4.applyFilter(newImage, image, PARALLELISM);

        DoubleThresholdFilter step5 = new DoubleThresholdFilter();
        step5.applyFilter(image, newImage, PARALLELISM);

        EdgeTrackingFilter step6 = new EdgeTrackingFilter();
        step6.applyFilter(newImage, image, PARALLELISM);

        Pair<Integer, Integer>[] ranges = ImageUtils.getRange(PARALLELISM, image.height);
        CompletableFuture<Void>[] partialFilters2 = new CompletableFuture[PARALLELISM];
        for (int i = 0; i < PARALLELISM; i++) {
            int start = ranges[i].getFirst();
            int stop = ranges[i].getSecond();

            partialFilters2[i] = CompletableFuture.runAsync(
                    () -> applyFilterPh1(image, newImage, start, stop));
        }
        CompletableFuture.allOf(partialFilters2).join();
    }

    public void applyFilterPh1(Image image, Image newImage, int start, int stop) {
        for (int i = start; i < stop; ++i) {
            final Pixel[] swp = image.matrix[i];
            image.matrix[i] = newImage.matrix[i];
            newImage.matrix[i] = swp;
            for (int j = 1; j < image.width - 1; ++j) {
                if (newImage.matrix[i][j].r < 100) {
                    newImage.matrix[i][j] = new Pixel((char) 0, (char) 0, (char) 0, newImage.matrix[i][j].a);
                }
            }
        }
    }
}
