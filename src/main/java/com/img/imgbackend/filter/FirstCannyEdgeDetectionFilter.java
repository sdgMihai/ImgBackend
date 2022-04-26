package com.img.imgbackend.filter;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;

public class FirstCannyEdgeDetectionFilter extends Filter{
    @Override
    public void applyFilter(Image image, Image newImage, int start, int stop) {
        // Clean noise
        for (int i = start; i < stop; ++i) {
            Pixel[] swp = image.matrix[i];
            image.matrix[i] = newImage.matrix[i];
            newImage.matrix[i] = swp;
            for (int j = 1; j < image.width - 1; ++j) {
                if (newImage.matrix[i][j].r < 100) {
                    newImage.matrix[i][j].r = 0;
                    newImage.matrix[i][j].g = 0;
                    newImage.matrix[i][j].b = 0;
                }
            }
        }
    }
}
