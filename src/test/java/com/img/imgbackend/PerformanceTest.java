package com.img.imgbackend;

import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.service.FilterService;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.GradientData;
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
import java.util.concurrent.*;

import static com.mongodb.assertions.Assertions.assertTrue;
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
@Slf4j
@Disabled
public class PerformanceTest {
    private final ImageFormatIO imageFormatIO  = new ImageFormatIO();

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
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public void testCannyEdgeDetectionFilter() {

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Callable<Object>> tasks = new ArrayList<>(NUM_THREADS);
        String[] filterNames = new String[]{Filters.CANNY_EDGE_DETECTION.toString()};

        final CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);
        final GradientData gData = new GradientData(input.height, input.width, NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier, input, output, filterNames.length, NUM_THREADS, filterNames, gData));

        ImgSrv imgSrv = new ImgSrv();

        for (int i = 0; i < NUM_THREADS; i++) {
            tasks.add(
                    Executors.callable(
                            new ImgSrv.SubImageFilter(
                                    FilterService.getFilters(filterNames
                                            , null
                                            , specificDataList.get(i))
                                    , specificDataList.get(i))));
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        try {
            assertTrue(executor.awaitTermination(40, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @TearDown(Level.Invocation)
    public void checkResult() {
        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
//        assertEquals(result, output);
    }

}
