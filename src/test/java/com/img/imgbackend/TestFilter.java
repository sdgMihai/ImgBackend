package com.img.imgbackend;

import com.img.imgbackend.filter.*;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@Import(
        value = {
                ImageFormatIO.class
        }
)
@Slf4j
public class TestFilter {

    private static Image input;

    @Autowired
    private ImageFormatIO imageFormatIO;

    private final int PARALLELISM = 4;

    @BeforeAll
    public static void init(@Autowired ImageFormatIO imageFormatIO) throws IOException {
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        byte[] image = Files.readAllBytes(imageFile.toPath());
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        input = imageFormatIO.bufferedToModelImage(bufferedImage);
    }

    @Test
    public void testBlackWhiteFilter() throws IOException {
        final Image output = new Image(input.width - 2, input.height - 2);
        BlackWhiteFilter blackWhiteFilter = new BlackWhiteFilter();

        blackWhiteFilter.applyFilter(input, output, PARALLELISM);

        File imageFile = new ClassPathResource("efficiencyBW.png").getFile();
        byte[] bwResponse = Files.readAllBytes(imageFile.toPath());
        BufferedImage bufferedBWResponse = ImageIO.read(new ByteArrayInputStream(bwResponse));
        final Image bwImage = imageFormatIO.bufferedToModelImage(bufferedBWResponse);

        Arrays.stream(output.matrix).flatMap(Stream::of).forEach(
                pixel -> assertThat(pixel.r
                        , anyOf(is(pixel.g)
                                , is(pixel.b)
                                , is(pixel.a)
                        ))
        );

        for (int i = 1; i < output.height - 1; i++) {
            for (int j = 1; j < output.width - 1; j++) {
                assertTrue((bwImage.matrix[i][j].r - output.matrix[i][j].r) <= 1
                        , String.format("This is more than floating point inexactitude at m[%d][%d]", i, j));

                assertTrue((bwImage.matrix[i][j].g - output.matrix[i][j].g) <= 1
                        , String.format("This is more than floating point inexactitude at m[%d][%d]", i, j));
            }
        }
    }

    @Test
    public void testSepia() throws IOException {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        SepiaFilter sepiaFilter = new SepiaFilter();

        sepiaFilter.applyFilter(input, output, PARALLELISM);

        //read result from memory
        File sepiaFile = new ClassPathResource("response.png").getFile();
        byte[] sepiaImage = Files.readAllBytes(sepiaFile.toPath());
        BufferedImage bufferedSepia = ImageIO.read(new ByteArrayInputStream(sepiaImage));
        final Image sepiaRes = imageFormatIO.bufferedToModelImage(bufferedSepia);

        assertEquals(sepiaRes.width, output.width);
        assertEquals(sepiaRes.height, output.height);
    }

    @Test
    public void testDummyFilter() {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        DummyFilter dummyFilter = new DummyFilter();

        dummyFilter.applyFilter(input, output, PARALLELISM);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testBrightnessFilter() {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        BrightnessFilter dummyFilter = new BrightnessFilter(0.2f);

        dummyFilter.applyFilter(input, output, PARALLELISM);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testContrastFilter() {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        ContrastFilter con = new ContrastFilter(0.1f);

        con.applyFilter(input, output, PARALLELISM);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testEdgeTrackingFilter() {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        EdgeTrackingFilter filter = new EdgeTrackingFilter();

        filter.applyFilter(input, output, PARALLELISM);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testEmbossFilter() {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        EmbossFilter embossFilter = new EmbossFilter();

        embossFilter.applyFilter(input, output, PARALLELISM);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testGaussianBlurFilter() throws IOException {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        GaussianBlurFilter filter = new GaussianBlurFilter();
        filter.applyFilter(input, output, PARALLELISM);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);

        File imageFile = new ClassPathResource("efficiencyGB.png").getFile();
        byte[] gbResponse = Files.readAllBytes(imageFile.toPath());
        BufferedImage bufferedGBResponse = ImageIO.read(new ByteArrayInputStream(gbResponse));
        final Image gbImage = imageFormatIO.bufferedToModelImage(bufferedGBResponse);

        for (int i = 1; i < output.height - 1; i++) {
            for (int j = 1; j < output.width - 1; j++) {
                assertTrue((gbImage.matrix[i][j].r - output.matrix[i][j].r) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].r", i, j));

                assertTrue((gbImage.matrix[i][j].g - output.matrix[i][j].g) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].g", i, j));

                assertTrue((gbImage.matrix[i][j].b - output.matrix[i][j].b) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].b", i, j));

                assertTrue((gbImage.matrix[i][j].a - output.matrix[i][j].a) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].a", i, j));
            }
        }
    }

    @Test
    public void testSharpenFilter() {
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        SharpenFilter filter = new SharpenFilter();

        filter.applyFilter(input, output, PARALLELISM);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }
}
