package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;

public interface Filter {
    void applyFilter(Image in, Image out, final int PARALLELISM);
}
