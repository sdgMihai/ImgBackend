package com.img.imgbackend.utils;

import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ImageUtils {
    public static void swap(Image in, Image out, int start, int stop) {
        IntStream.range(start, stop).forEach(i -> {
                    var tmp = in.matrix[i];
                    in.matrix[i] = out.matrix[i];
                    out.matrix[i] = tmp;
                });
    }

    public static void swap(Image in, Image out, int PARALLELISM) {
        final Pair<Integer, Integer>[] range = getRange(PARALLELISM, in.height);
        Arrays.stream(range)
                .parallel()
                .forEach(rangeIt -> swap(in, out, rangeIt.getFirst(), rangeIt.getSecond()));
    }

    public static Pair<Integer, Integer>[] getRange(int PARALLELISM, int imgH) {
        Pair<Integer, Integer>[] res = new Pair[PARALLELISM];
        for (int i = 0; i < PARALLELISM; i++) {
            int slice = (imgH - 2) / PARALLELISM;
            int start = Math.max(1, i * slice);
            int stop = (i + 1) * slice;
            if (i + 1 == PARALLELISM) {
                stop = Math.max((i + 1) * slice, imgH - 1);
            }
            res[i] = Pair.of(start, stop);
        }
        return res;
    }
}
