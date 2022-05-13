package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;

import java.util.concurrent.BrokenBarrierException;

public class CannyEdgeDetectionFilter extends Filter {
    private static float[][] auxTheta;

    public CannyEdgeDetectionFilter() {
        this.filter_additional_data = null;
    }
    public CannyEdgeDetectionFilter(FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
    }
    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start    first line to be processed from input image.
     * @param stop     past last line to be processed from input image.
     */
    @Override
    public void applyFilter(Image image, Image newImage, int start, int stop) throws BrokenBarrierException, InterruptedException {
        ThreadSpecificDataT tData = (ThreadSpecificDataT) filter_additional_data;

        for (int i = start; i < stop; ++i) {
            for (int j = 0; j < image.width - 1; ++j) {
                int gray = (int) (0.2126 * image.matrix[i][j].r +
                        0.7152 * image.matrix[i][j].g +
                        0.0722 * image.matrix[i][j].b);
                gray = Math.min(gray, 255);
                newImage.matrix[i][j] = new Pixel((char) gray, (char) gray, (char) gray, image.matrix[i][j].a);
            }
        }

        BlackWhiteFilter step1 = new BlackWhiteFilter();
        step1.applyFilter(image, newImage, 0, image.height - 1);
        tData.barrier.await();

        GaussianBlurFilter step2 = new GaussianBlurFilter();
        step2.applyFilter(newImage, image, 1, newImage.height - 1);
        tData.barrier.await();

        GradientFilter step3 = new GradientFilter(tData);
        step3.applyFilter(image, newImage, 1, newImage.height - 1);
        if (tData.threadID == 0) {
            auxTheta = step3.theta;
        }
        tData.barrier.await();

        NonMaximumSuppressionFilter step4 = new NonMaximumSuppressionFilter(auxTheta, step3.thetaHeight, step3.thetaWidth, tData);
        step4.applyFilter(newImage, image, 1, newImage.height - 1);
        tData.barrier.await();

        DoubleThresholdFilter step5 = new DoubleThresholdFilter(tData);
        step5.applyFilter(image, newImage, 1, newImage.height - 1);
        tData.barrier.await();

        EdgeTrackingFilter step6 = new EdgeTrackingFilter();
        step6.applyFilter(newImage, image, 1, newImage.height - 1);
        tData.barrier.await();

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
