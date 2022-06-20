package com.img.imgbackend;

import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.Barrier;
import com.img.imgbackend.utils.DataInit;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.*;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

@State(Benchmark)
@ExtendWith(SpringExtension.class)
@Import(
        value = {
                ImageFormatIO.class
        }
)
@Disabled
@Slf4j
public class PerformanceTest {
    private ImageFormatIO imageFormatIO  = new ImageFormatIO();
    private static final int NUM_THREADS = 4;

    Image input;
    Image output;
    Image result;

    @Setup(Level.Invocation)
    public void init() throws IOException {
        File inputFile = new ClassPathResource("noise.png").getFile();
        byte[] image = Files.readAllBytes(inputFile.toPath());
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        input = imageFormatIO.bufferedToModelImage(bufferedImage);
        assertNotNull(input);
        output = new Image(input.width - 2, input.height - 2);
        assertNotNull(output);

        File outputResult = new ClassPathResource("respnoise.png").getFile();
        byte[] resultBytes = Files.readAllBytes(outputResult.toPath());
        result = imageFormatIO.bufferedToModelImage(
                ImageIO.read(new ByteArrayInputStream(
                        resultBytes
                ))
        );
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 4)
    public void testCannyEdgeDetectionFilter() {

        List<Thread> tasks = new ArrayList<>(NUM_THREADS);
        List<String> filter = List.of(Filters.CANNY_EDGE_DETECTION.toString());
        Object lock = new Object();
        DataInit dataInit = new DataInit();

        final Barrier barrier = new Barrier(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier,lock, input, output, filter.size(), NUM_THREADS, filter, dataInit));

        ImgSrv imgSrv = new ImgSrv();

        for (int i = 0; i < NUM_THREADS; i++) {
            tasks.add(imgSrv.new SubImageFilter(specificDataList.get(i)));
            tasks.get(i).start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                tasks.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @TearDown(Level.Invocation)
    public void checkResult() {
        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
        assertEquals(result, output);
    }


}
