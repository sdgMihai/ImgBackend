package com.img.imgbackend;

import com.img.imgbackend.filter.*;
import com.img.imgbackend.model.ImgBin;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.repository.ImageRepository;
import com.img.imgbackend.utils.Barrier;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
@AutoConfigureMockMvc
public class TestFilter {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ImageRepository repository;

    @Autowired
    private ImageFormatIO imageFormatIO;

    private Barrier cyclicBarrier;

    private Lock lock;

    private int NUM_THREADS;

    private static final Logger log = LogManager.getLogger(TestFilter.class);



    @BeforeEach
    public void init() throws IOException {
        // provide image as input for tests
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        byte[] image = Files.readAllBytes(imageFile.toPath());
        ImgBin imgBinOne = new ImgBin("1", new Binary(image));
        Mockito.when(repository.findById("1"))
                .thenReturn(Optional.of(imgBinOne));

        NUM_THREADS = 1;
        cyclicBarrier = new Barrier(NUM_THREADS);
        lock = new ReentrantLock();
    }

    @Test
    public void testBlackWhiteFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
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
        // read input image
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        SepiaFilter sepiaFilter = new SepiaFilter(addData);

        sepiaFilter.applyFilter(input, output);

        //read result from memory
        File sepiaFile = new ClassPathResource("response.png").getFile();
        byte[] sepiaImage = Files.readAllBytes(sepiaFile.toPath());
        BufferedImage bufferedSepia = ImageIO.read(new ByteArrayInputStream(sepiaImage));
        final Image sepiaRes = imageFormatIO.bufferedToModelImage(bufferedImage);

        assertEquals(sepiaRes.width, output.width);
        assertEquals(sepiaRes.height, output.height);
    }

    @Test
    public void testDummyFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        DummyFilter dummyFilter = new DummyFilter(addData);

        dummyFilter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testBrightnessFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        BrightnessFilter dummyFilter = new BrightnessFilter(0.2f, addData);

        dummyFilter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testCannyEdgeDetectionFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
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
    public void testContrastFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        ContrastFilter con = new ContrastFilter(0.1f, addData);

        con.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testDoubleThresholdFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
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
    public void testEdgeTrackingFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        EdgeTrackingFilter filter = new EdgeTrackingFilter(addData);

        filter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testEmbossFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        EmbossFilter embossFilter = new EmbossFilter(addData);

        embossFilter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testGaussianBlurFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        GaussianBlurFilter filter = new GaussianBlurFilter(addData);

        filter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }

    @Test
    public void testGradientFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
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
    public void testNonMaFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter + gradient filter, bc non-maximum needs the theta obtained from it
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
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
    public void testSharpenFilter() throws IOException {
        byte[] image = repository.findById("1").get().binary().getData();
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        // create additional data and filter
        FilterAdditionalData addData = new ThreadSpecificDataT(0, cyclicBarrier, lock, NUM_THREADS);
        SharpenFilter filter = new SharpenFilter(addData);

        filter.applyFilter(input, output);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
    }
}
