package com.img.imgbackend.utils;

public class GradientData {
    public volatile float[][] Ix;
    public volatile float[][] Iy;
    public volatile float[][] theta;

    public GradientData(int imgHeight, int imgWidth, int CONSUMER_THREADS) {
        Ix = new float[imgHeight][];
        Iy = new float[imgHeight][];
        theta = new float[imgHeight][];

        for (int i = 0; i < imgHeight; ++i) {
            Ix[i] = new float[imgWidth];
            Iy[i] = new float[imgWidth];
            theta[i] = new float[imgWidth];
        }
    }
}
