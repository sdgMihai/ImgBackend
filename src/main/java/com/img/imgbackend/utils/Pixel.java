package com.img.imgbackend.utils;

import java.util.Objects;

public class Pixel {
    public char r;
    public char g;
    public char b;
    public char a;

    /**
     * constructor - creeaza un pixel-zero (r = g = b = a = 0)
     */
    public Pixel() {
        this.r = this.g = this.b = this.a = 0;
    }

    /**
     * constructor - creeaza un obiect pixel cu cele trei componente
     *
     * @param r componenta red
     * @param g componenta green
     * @param b componenta blue
     * @param a componenta alfa
     */
    public Pixel(char r, char g, char b, char a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pixel pixel = (Pixel) o;
        return r == pixel.r && g == pixel.g && b == pixel.b && a == pixel.a;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }

    @Override
    public String toString() {
        return "Pixel{" +
                "r=" + (int)r +
                ", g=" + (int)g +
                ", b=" + (int)b +
                ", a=" + (int)a +
                '}';
    }
}
