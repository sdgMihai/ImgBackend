package com.img.imgbackend.utils;

import java.util.stream.IntStream;

public class ImageUtils {
    public static void swap(Image in, Image out, int start, int stop) {
        IntStream.range(start, stop).forEach(i -> {
                    var tmp = in.matrix[i];
                    in.matrix[i] = out.matrix[i];
                    out.matrix[i] = tmp;
                });
    }

}
