package com.img.imgbackend;

import com.img.imgbackend.filter.*;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.utils.GradientData;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@Import(
        value = {
                ImageFormatIO.class
        }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class TestFilter {

    private static final Logger log = LogManager.getLogger(TestFilter.class);
    FilterAdditionalData addData;
    @Autowired
    private ImageFormatIO imageFormatIO;
    private CyclicBarrier cyclicBarrier;
    private int NUM_THREADS;
    private GradientData gData;
    private Image input;
    private Image output;

    @BeforeEach
    public void init() throws IOException {
        // provide image as input for tests
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        byte[] image = Files.readAllBytes(imageFile.toPath());
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        input = imageFormatIO.bufferedToModelImage(bufferedImage);
        output = new Image(input.width - 2, input.height - 2);

        NUM_THREADS = 1;
        cyclicBarrier = new CyclicBarrier(NUM_THREADS);

        gData = new GradientData(input.height, input.width, NUM_THREADS);
        addData = new ThreadSpecificDataT(0, cyclicBarrier, NUM_THREADS, gData);
    }

    @Test
    public void testBlackWhiteFilter() {
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, NUM_THREADS, gData);
        BlackWhiteFilter blackWhiteFilter = new BlackWhiteFilter(addData);

        blackWhiteFilter.applyFilter(input, output);

        Arrays.stream(output.matrix).flatMap(Stream::of).forEach(
                pixel -> assertThat(pixel.r
                        , anyOf(is(pixel.g)
                                , is(pixel.b)
                                , is(pixel.a)
                        ))
        );
    }

    @Test
    public void testSepia() throws IOException {

        // create additional data and filter
        SepiaFilter sepiaFilter = new SepiaFilter(addData);

        sepiaFilter.applyFilter(input, output);

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
        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, NUM_THREADS, gData);
        DummyFilter dummyFilter = new DummyFilter(addData);

        dummyFilter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testBrightnessFilter() {
        BrightnessFilter dummyFilter = new BrightnessFilter(0.2f, addData);

        dummyFilter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testCannyEdgeDetectionFilter() {
        CannyEdgeDetectionFilter cannyEdgeDetectionFilter = new CannyEdgeDetectionFilter(addData);

        try {
            cannyEdgeDetectionFilter.applyFilter(input, output);
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testContrastFilter() {
        ContrastFilter con = new ContrastFilter(0.1f, addData);

        con.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testDoubleThresholdFilter() {
        DoubleThresholdFilter filter = new DoubleThresholdFilter(addData);

        try {
            filter.applyFilter(input, output);
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testEdgeTrackingFilter() {
        EdgeTrackingFilter filter = new EdgeTrackingFilter(addData);

        filter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testEmbossFilter() {
        EmbossFilter embossFilter = new EmbossFilter(addData);

        embossFilter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testGaussianBlurFilter() {
        GaussianBlurFilter filter = new GaussianBlurFilter(addData);

        filter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testGradientFilter() {
        GradientFilter filter = new GradientFilter(addData);

        try {
            filter.applyFilter(input, output);
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testNonMaFilter() {
        GradientFilter gradient = new GradientFilter(addData);
        try {
            gradient.applyFilter(input, output);
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        NonMaximumSuppressionFilter filter = new NonMaximumSuppressionFilter(gradient.theta, 1, 1, addData);
        filter.applyFilter(output, input);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testSharpenFilter() {
        SharpenFilter filter = new SharpenFilter(addData);

        filter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }
}
