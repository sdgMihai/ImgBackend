package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ImageUtils;
import com.img.imgbackend.utils.Pixel;
import org.springframework.data.util.Pair;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class DoubleThresholdFilter implements Filter{

    /**
     * @param in          input image reference.
     * @param out         output image reference.
     * @param PARALLELISM integer value denoting the number of task running in parallel.
     */
    public void applyFilter(Image in, Image out, final int PARALLELISM) {
        CompletableFuture<Float>[] partialFilters = new CompletableFuture[PARALLELISM];
        Pair<Integer, Integer>[] ranges = ImageUtils.getRange(PARALLELISM, in.height);
        for (int i = 0; i < PARALLELISM; i++) {
            int start = ranges[i].getFirst();
            int stop = ranges[i].getSecond();
            partialFilters[i] = CompletableFuture.supplyAsync(() -> applyFilterPh1(in, start, stop));
        }
        final Optional<Float> maxVal = Stream.of(partialFilters)
                .map(CompletableFuture::join)
                .max(Float::compareTo);
        CompletableFuture<Void>[] partialFilters2 = new CompletableFuture[PARALLELISM];
        for (int i = 0; i < PARALLELISM; i++) {
            int start = ranges[i].getFirst();
            int stop = ranges[i].getSecond();

            partialFilters2[i] = CompletableFuture.runAsync(
                    () -> applyFilterPh2(in, out, start, stop, maxVal.get()));
        }
        CompletableFuture.allOf(partialFilters2).join();
    }

    public float applyFilterPh1(Image image, int start, int stop) {
        float threadMaxVal = -3.40282347e+38F;
        for (int i = start; i < stop; ++i) {
            for (int j = 1; j < image.width - 1; ++j) {
                threadMaxVal = (threadMaxVal < image.matrix[i][j].r) ? image.matrix[i][j].r : threadMaxVal;
            }
        }
        return threadMaxVal;
    }

    public void applyFilterPh2(Image image, Image newImage, int start, int stop, float maxVal) {
        float thresholdHigh = 0.06f;
        float high = maxVal * thresholdHigh;
        float thresholdLow = 0.05f;
        float low = high * thresholdLow;

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
