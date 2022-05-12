package com.img.imgbackend.repository;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.Pixel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

@Slf4j
@Service
public class ImageFormatIO {
    public Image bufferedToModelImage(BufferedImage buff) {
        Image res = new Image(buff.getWidth(), buff.getHeight());

            for (int i = 0; i < buff.getHeight(); ++i) {
                for (int j = 0; j < buff.getWidth(); j++) {
                    int rgb = buff.getRGB(j, i);
                    int red = (rgb >> 16) & 0x000000FF;
                    int green = (rgb >> 8) & 0x000000FF;
                    int blue = (rgb) & 0x000000FF;
                    int alpha = (rgb >> 24) & 0x000000FF;
                    res.matrix[i + 1][j + 1] = new Pixel((char) red, (char) green, (char) blue, (char) alpha);
                }
            }

        return res;
    }

    public BufferedImage modelToBufferedImage(Image image) {
        log.debug("model to Buffered Image");
        BufferedImage res = new BufferedImage(image.width - 2, image.height - 2, TYPE_4BYTE_ABGR);
        for (int i  = 1; i < image.height - 1; ++i) {
            for (int j = 1; j < image.width - 1; j++) {
                Pixel pixelC = image.matrix[i][j];
                int pixelI;
                int red = pixelC.r;
                int green = pixelC.g;
                int blue = pixelC.b;
                int alpha = pixelC.a;
                pixelI = (alpha << 24) | (red << 16) | (green << 8) | blue ;
                res.setRGB(j - 1, i - 1, pixelI);
            }
        }
        return res;
    }

    public byte[] bufferedToByteArray(BufferedImage buff) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(buff, "png", baos);
        return baos.toByteArray();
    }
}
