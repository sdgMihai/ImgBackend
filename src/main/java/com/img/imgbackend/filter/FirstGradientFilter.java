package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record FirstGradientFilter(float[][] theta, float[][] ix, float[][] iy) {
    private static final float[][] Gx = new float[][]{{-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}};

    private static final float[][] Gy = new float[][]{{1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}};

    public Double applyFilter(Image image, int start, int stop) {

        // 1.  Gx is applied on image -> generate ix
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float gray = 0;

                for (int ki = -1; ki <= 1; ++ki) {
                    for (int kj = -1; kj <= 1; ++kj) {
                        gray += (float) (image.matrix[i + ki][j + kj].r) * Gx[ki + 1][kj + 1];
                    }
                }
                ix[i][j] = gray;
            }
        }

        // 2. Gy is applied on image -> generate iy
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float gray = 0;

                for (int ki = -1; ki <= 1; ++ki) {
                    for (int kj = -1; kj <= 1; ++kj) {
                        gray += (float) (image.matrix[i + ki][j + kj].r) * Gy[ki + 1][kj + 1];
                    }
                }
                iy[i][j] = gray;
            }
        }

        double threadgMax = -3.40282347e+38F;
        // 3. calculate G = sqrt(Gx**2 + Gy**2) for each elem, using ix as accumulator
        // calculate theta = arctangent(iy, ix) for each elem
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                float gray;
                gray = (float) Math.sqrt(ix[i][j] * ix[i][j] + iy[i][j] * iy[i][j]);
                if (threadgMax < gray) {
                    threadgMax = gray;
                }
                this.theta[i][j] = (float) (Math.atan2(iy[i][j], ix[i][j]) * 180 / Math.PI);
                ix[i][j] = gray;
                if (this.theta[i][j] < 0) {
                    this.theta[i][j] = this.theta[i][j] + 180;
                }
            }
        }
        return threadgMax;
    }
}
