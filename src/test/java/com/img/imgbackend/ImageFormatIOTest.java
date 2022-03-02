package com.img.imgbackend;

import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageFormatIOTest {
    @Test
    public void testChangeImageFormat() throws IOException {
        Image input = new Image(1, 1);
        ImageFormatIO imageFormatIO = new ImageFormatIO();

        char r = 1;
        char g = 2;
        char b = 3;
        char a = 0;
        input.matrix[1][1] = new Pixel(r, g, b, a);

        final BufferedImage bufferedImage = imageFormatIO.modelToBufferedImage(input);
        final int rgb = bufferedImage.getRGB(0, 0);
        assertEquals(r, rgb  >> 16);
        assertEquals(g, (rgb  >> 8) & 0xff);
        assertEquals(b, (rgb  >> 0) & 0xff);
        assertEquals(a, (rgb  >> 24) & 0xff);

        final Image toModelImage = imageFormatIO.bufferedToModelImage(bufferedImage);
        assertEquals(input.matrix[1][1].r, toModelImage.matrix[1][1].r);
        assertEquals(input.matrix[1][1].g, toModelImage.matrix[1][1].g);
        assertEquals(input.matrix[1][1].b, toModelImage.matrix[1][1].b);
        assertEquals(input.matrix[1][1].a, toModelImage.matrix[1][1].a);

        final byte[] toByteArray = imageFormatIO.bufferedToByteArray(bufferedImage);
        BufferedImage tmpBuff = ImageIO.read(new ByteArrayInputStream(toByteArray));
        final int rgbTMP = bufferedImage.getRGB(0, 0);
        assertEquals(r, rgbTMP  >> 16);
        assertEquals(g, (rgbTMP  >> 8) & 0xff);
        assertEquals(b, (rgbTMP  >> 0) & 0xff);
        assertEquals(a, (rgbTMP  >> 24) & 0xff);
    }
}
