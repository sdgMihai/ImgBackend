package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ImageUtils;
import com.img.imgbackend.utils.Pixel;
import org.springframework.data.util.Pair;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class GradientFilter implements Filter {
    public float[][] theta; /* place to save theta calculation */
    public int thetaHeight;
    public int thetaWidth;
    private static final float[][] Gx = new float[][]{{-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}};

    private static final float[][] Gy = new float[][]{{1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}};

    private static float[][] Ix;
    private static float[][] Iy;

    /**
     * @param in          input image reference.
     * @param out         output image reference.
     * @param PARALLELISM integer value denoting the number of task running in parallel.
     */
    @Override
    public void applyFilter(Image in, Image out, final int PARALLELISM) {
        Ix = new float[in.height][];
        Iy = new float[in.height][];
        float[][] auxTheta = new float[in.height][];

        for (int i = 0; i < in.height; ++i) {
            Ix[i] = new float[in.width];
            Iy[i] = new float[in.width];
            auxTheta[i] = new float[in.width];
        }


        this.thetaHeight = in.height;
        this.thetaWidth = in.width;

        this.theta = auxTheta;

        // ph1
        CompletableFuture<Float>[] partialFilters = new CompletableFuture[PARALLELISM];
        Pair<Integer, Integer>[] ranges = ImageUtils.getRange(PARALLELISM, in.height);
        for (int i = 0; i < PARALLELISM; i++) {
            int start = ranges[i].getFirst();
            int stop = ranges[i].getSecond();
            partialFilters[i] = CompletableFuture.supplyAsync(() -> applyFilterPh1(in, out, start, stop));
        }
        final Optional<Float> gMax = Stream.of(partialFilters)
                .map(CompletableFuture::join)
                .max(Float::compareTo);
        // ph2
        CompletableFuture<Void>[] partialFilters2 = new CompletableFuture[PARALLELISM];
        for (int i = 0; i < PARALLELISM; i++) {
            int start = ranges[i].getFirst();
            int stop = ranges[i].getSecond();

            partialFilters2[i] = CompletableFuture.runAsync(
                    () -> applyFilterPh2(in, out, start, stop, gMax.get()));
        }
        CompletableFuture.allOf(partialFilters2).join();
    }

    public float applyFilterPh1(Image image, Image newImage, int start, int stop) {
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
        return threadgMax;
    }


    public void applyFilterPh2(Image image, Image newImage, int start, int stop, float gMax) {
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
    }
}
