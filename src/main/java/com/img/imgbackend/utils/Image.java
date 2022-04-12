package com.img.imgbackend.utils;

import java.util.Arrays;
import java.util.Objects;

public class Image {
    public int width;
    public int height;
    public Pixel[][] matrix;

    /**
     * constructor - aloca memorie pentru o imagine bordata cu pixel-zero(r = g = b = a = 0)
     * OBS: Imaginea initial va contine doar pixeli-zero.
     * Ea va fi populata cand se vor citii datele din fisier(deci in ImageIO
     * functia de imageRead va popula imaginea)
     * @param width latime imagine
     * @param height inaltime imagine
     */
    public Image(int width,int height) {
        this.width = width + 2;
        this.height = height + 2;
        this.matrix = new Pixel[this.height][this.width];

        // bordering with '0' pixel
        for (int i = 0; i < this.width; ++i) {
            this.matrix[0][i] = new Pixel((char) 0, (char) 0, (char) 0, (char) 0);
            this.matrix[this.height - 1][i] = new Pixel((char) 0, (char) 0, (char) 0, (char) 0);
        }
        for (int i = 0; i < this.height; ++i) {
            this.matrix[i][0] = new Pixel((char) 0, (char) 0, (char) 0, (char) 0);
            this.matrix[i][this.width - 1] = new Pixel((char) 0, (char) 0, (char) 0, (char) 0);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return width == image.width && height == image.height && Arrays.deepEquals(matrix, image.matrix);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(width, height);
        result = 31 * result + Arrays.deepHashCode(matrix);
        return result;
    }
}
