package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import com.img.imgbackend.utils.ThreadSpecificDataT;

import java.util.concurrent.BrokenBarrierException;

public class DoubleThresholdFilter extends Filter {

    private final float thresholdHigh = 0.06f;
    private final float thresholdLow = 0.05f;
    private static volatile float maxVal = -3.40282347e+38F;

    public DoubleThresholdFilter() {
        this.filter_additional_data = null;
    }

    public DoubleThresholdFilter(FilterAdditionalData filter_additional_data) {
        this.filter_additional_data = filter_additional_data;
    }

    /**
     * @param image    referinta catre imagine
     * @param newImage referinta catre obiectul tip Image
     *                 care va contine imaginea rezultata in urma
     *                 aplicarii filtrului.
     */
    @Override
    public void applyFilter(Image image, Image newImage) throws BrokenBarrierException, InterruptedException {
        ThreadSpecificDataT tData = (ThreadSpecificDataT) filter_additional_data;
        int slice = (image.height - 2) / tData.NUM_THREADS;  // imaginea va avea un rand de pixeli deasupra si unul dedesubt
        //de aici '-2' din ecuatie
        int start = Math.max(1, tData.threadID * slice);
        int stop = (tData.threadID + 1) * slice;
        if (tData.threadID + 1 == tData.NUM_THREADS) {
            stop = Math.max((tData.threadID + 1) * slice, image.height - 1);
        }

        float threadMaxVal = -3.40282347e+38F;
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                threadMaxVal = (threadMaxVal < image.matrix[i][j].r) ? image.matrix[i][j].r : threadMaxVal;
            }
        }

        // Compare and set to get the maximum value
        synchronized (tData.mutex) {
            maxVal = Math.max(maxVal, threadMaxVal);
        }
        tData.barrier.await();

        float high = maxVal * this.thresholdHigh;
        float low = high * this.thresholdLow;

        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                if (image.matrix[i][j].r >= high) {
                    newImage.matrix[i][j] = new Pixel((char) 255, (char) 255, (char) 255, image.matrix[i][j].a);
                } else {
                    if (image.matrix[i][j].r >= low) {
                        newImage.matrix[i][j] = new Pixel((char) 100, (char) 100, (char) 100, image.matrix[i][j].a);
                    } else newImage.matrix[i][j] = new Pixel((char) 0, (char) 0, (char) 0, image.matrix[i][j].a);
                }
            }
        }
    }
}
