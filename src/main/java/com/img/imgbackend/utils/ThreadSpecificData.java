package com.img.imgbackend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThreadSpecificData {
    final int PARALLELISM;
    Image image;
    Image newImage;
}
