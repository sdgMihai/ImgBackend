package com.img.imgbackend;

import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.service.FilterService;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Import(
        value = {
                ImageFormatIO.class
        }
)
@Slf4j
public class PerformanceTest {
    private static final int NUM_THREADS = 4;
    @Autowired
    private ImageFormatIO imageFormatIO;

    @RepeatedTest(2)
    public void testCannyEdgeDetectionFilter() throws IOException {
        File inputFile = new ClassPathResource("noise.png").getFile();
        byte[] image = Files.readAllBytes(inputFile.toPath());
        assert (image.length != 0);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
        final Image output = new Image(input.width - 2, input.height - 2);

        File outputResult = new ClassPathResource("respnoise.png").getFile();
        byte[] resultBytes = Files.readAllBytes(outputResult.toPath());
        final Image result = imageFormatIO.bufferedToModelImage(
                ImageIO.read(new ByteArrayInputStream(
                        resultBytes
                ))
        );

        List<Thread> tasks = new ArrayList<>(NUM_THREADS);
        String[] filterNames = new String[]{Filters.CANNY_EDGE_DETECTION.toString()};
        Lock lock = new ReentrantLock();

        final CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier, lock, input, output, filterNames.length, NUM_THREADS, filterNames));

        ImgSrv imgSrv = new ImgSrv();

        for (int i = 0; i < NUM_THREADS; i++) {
            tasks.add(
                    new ImgSrv.SubImageFilter(
                            FilterService.getFilters(filterNames
                                    , null
                                    , specificDataList.get(i))
                            , specificDataList.get(i)));
            tasks.get(i).start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                tasks.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
        assertEquals(result, output);
    }

}
