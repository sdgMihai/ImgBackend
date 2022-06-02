package com.img.imgbackend.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataInit {
    private GradientData gradientRes = null;
    private volatile int consumers;

    public synchronized void producerGradient(int imgHeight, int imgWidth, int CONSUMER_THREADS) {
        assert imgHeight != 0;
        assert imgWidth != 0;
        assert CONSUMER_THREADS != 0;
        gradientRes = new GradientData();
        gradientRes.Ix = new float[imgHeight][];
        gradientRes.Iy = new float[imgHeight][];
        gradientRes.theta = new float[imgHeight][];

        for (int i = 0; i < imgHeight; ++i) {
            gradientRes.Ix[i] = new float[imgWidth];
            gradientRes.Iy[i] = new float[imgWidth];
            gradientRes.theta[i] = new float[imgWidth];
        }
        consumers = CONSUMER_THREADS;
        notifyAll();
    }

    public synchronized GradientData consumerGradient() {
        if (gradientRes == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        GradientData data = gradientRes;
        assert data.Ix != null;
        assert data.Ix[0] != null;
        consumers--;
        if (consumers == 0) {
            gradientRes = null;
        } else {
            notifyAll();
        }

        return data;
    }
}
