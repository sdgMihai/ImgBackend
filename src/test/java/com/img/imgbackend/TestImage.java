package com.img.imgbackend;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TestImage {
    @Test
    public void create() {
        int width = 20;
        int height = 30;
        Image image = new Image(width, height);
        assertEquals(width + 2, image.width);  //  width + 0-ed borders
        assertEquals(height + 2, image.height);
    }

    @Test
    public void testPixel() {
        char r = 0;
        char g = 1;
        char b = 2;
        char a = 3;
        Pixel pixel = new Pixel(r, g, b, a);
        assertEquals(r, pixel.r);
        assertEquals(g, pixel.g);
        assertEquals(b, pixel.b);
        assertEquals(a, pixel.a);
    }

}
