package com.img.imgbackend.filter;

import com.img.imgbackend.utils.*;

import java.util.concurrent.BrokenBarrierException;

public class GradientFilter extends Filter {
    public float[][] theta; /* place to save theta calculation */
    public float[][] Ix;
    public float[][]  Iy;
    public int thetaHeight;
    public int thetaWidth;
    private static final float[][] Gx = new float[][]{{-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}};

    private static final float[][] Gy = new float[][]{{1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}};

    private static volatile float gMax = -3.40282347e+38F;

    public GradientFilter() {
        this.filter_additional_data = null;
    }

    public GradientFilter(FilterAdditionalData filter_additional_data) {
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
        DataInit dataInit = tData.dataInit;
        int slice = (image.height - 2) / tData.NUM_THREADS;//imaginea va avea un rand de pixeli deasupra si unul dedesubt
        //de aici '-2' din ecuatie
        int start = Math.max(1, tData.threadID * slice);
        int stop = (tData.threadID + 1) * slice;
        if (tData.threadID + 1 == tData.NUM_THREADS) {
            stop = Math.max((tData.threadID + 1) * slice, image.height - 1);
        }

        this.thetaHeight = image.height;
        this.thetaWidth = image.width;
        if (tData.threadID == 0) {
            dataInit.producerGradient(image.height, image.width, tData.NUM_THREADS);

        }
        final GradientData gradientData = dataInit.consumerGradient();
        this.Ix = gradientData.Ix;
        this.Iy = gradientData.Iy;
        this.theta = gradientData.theta;

        // 1. Se aplica kernelul Gx pe imagine si se obtine Ix
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float gray = 0;

                for (int ki = -1; ki <= 1; ++ki) {
                    for (int kj = -1; kj <= 1; ++kj) {
                        gray += (float) (image.matrix[i + ki][j + kj].r) * Gx[ki + 1][kj + 1];
                    }
                }
                Ix[i][j] = gray;
            }
        }

        // 2. Se aplica kernelul Gy pe imagine si se obtine Iy
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float gray = 0;

                for (int ki = -1; ki <= 1; ++ki) {
                    for (int kj = -1; kj <= 1; ++kj) {
                        gray += (float) (image.matrix[i + ki][j + kj].r) * Gy[ki + 1][kj + 1];
                    }
                }
                Iy[i][j] = gray;
            }
        }

        float threadgMax = -3.40282347e+38F;
        // 3. Se calculeaza G = sqrt(Gx**2 + Gy**2) pe fiecare element, se foloseste Ix ca depozit
        // Se calculeaza theta = arctangenta(Iy, Ix) pe fiecare element
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float gray;
                gray = (float)Math.sqrt(Ix[i][j] * Ix[i][j] + Iy[i][j] * Iy[i][j]);
                if (threadgMax < gray) {
                    threadgMax = gray;
                }
                this.theta[i][j] = (float) (Math.atan2(Iy[i][j], Ix[i][j]) * 180 / Math.PI);
                Ix[i][j] = gray;
                if (this.theta[i][j] < 0) {
                    this.theta[i][j] =  this.theta[i][j] + 180;
                }
            }
        }

        synchronized (tData.mutex) {
            gMax = Math.max(gMax, threadgMax);
        }
        tData.barrier.await();

        // 4. Se calculeaza G = G / G.max() * 255
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float gray = Ix[i][j];
                gray = (gray / gMax) * 255;
                gray = (gray < 0) ? 0 : gray;
                gray = (gray > 255) ? 255 : gray;
                newImage.matrix[i][j] = new Pixel((char) gray, (char) gray, (char) gray, image.matrix[i][j].a);
            }
        }

        tData.barrier.await();
    }
}
