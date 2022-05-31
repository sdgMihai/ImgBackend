package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;

import java.util.concurrent.BrokenBarrierException;

public class CannyEdgeDetectionFilter extends Filter {
    private int rank;
    private int numtasks;
    private int chunk;
    private static float[][] auxTheta;

    public CannyEdgeDetectionFilter() {
        this.filter_additional_data = null;
    }
    public CannyEdgeDetectionFilter(FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
    }
    /**
     * @param image referinta catre imagine
     * @param newImage referinta catre obiectul tip Image
     *          care va contine imaginea rezultata in urma
     *          aplicarii filtrului.
     */
    @Override
    public void applyFilter(Image image, Image newImage) throws BrokenBarrierException, InterruptedException {
        ThreadSpecificDataT tData = (ThreadSpecificDataT) filter_additional_data;

        BlackWhiteFilter step1 = new BlackWhiteFilter(tData);
        step1.applyFilter(image, newImage);
        tData.barrier.await();

        GaussianBlurFilter step2 = new GaussianBlurFilter(tData);
        step2.applyFilter(newImage, image);
        tData.barrier.await();

        GradientFilter step3 = new GradientFilter(tData);
        step3.applyFilter(image, newImage);
        if (tData.threadID == 0) {
            auxTheta = step3.theta;
        }
        tData.barrier.await();

        NonMaximumSuppressionFilter step4 = new NonMaximumSuppressionFilter(auxTheta, step3.thetaHeight, step3.thetaWidth, tData);
        step4.applyFilter(newImage, image);
        tData.barrier.await();

        DoubleThresholdFilter step5 = new DoubleThresholdFilter(tData);
        step5.applyFilter(image, newImage);
        tData.barrier.await();

        EdgeTrackingFilter step6 = new EdgeTrackingFilter(tData);
        step6.applyFilter(newImage, image);
        tData.barrier.await();

        int slice = (image.height - 2) / tData.NUM_THREADS;//imaginea va avea un rand de pixeli deasupra si unul dedesubt
        //de aici '-2' din ecuatie
        int start = Math.max(1, tData.threadID * slice);
        int stop = (tData.threadID + 1) * slice;
        if (tData.threadID + 1 == tData.NUM_THREADS) {
            stop = Math.max((tData.threadID + 1) * slice, image.height - 1);
        }

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
