package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;

public abstract class Filter {
    /**
     * @param image    input image reference.
     * @param newImage output image reference.
     * @param start first line to be processed from input image.
     * @param stop past last line to be processed from input image.
     */
    public abstract void applyFilter(Image image, Image newImage, int start, int stop);

}
