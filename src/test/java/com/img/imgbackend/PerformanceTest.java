package com.img.imgbackend;

import com.img.imgbackend.filter.CannyEdgeDetectionFilter;
import com.img.imgbackend.filter.FilterAdditionalData;
import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.model.ImgBin;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.repository.ImageRepository;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificData;
import com.img.imgbackend.utils.ThreadSpecificDataT;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Import(
        value = {
                ImageFormatIO.class
        }
)
@Slf4j
public class PerformanceTest {
    @Autowired
    private ImageFormatIO imageFormatIO;

    private static final int NUM_THREADS = 4;

    @Test
    public void testCannyEdgeDetectionFilter() throws IOException {
        log.debug("start canny performance test!!!!!!!!!!!!!");
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
        List<String> filter = List.of(Filters.CANNY_EDGE_DETECTION.toString());
        Lock lock = new ReentrantLock();

        final CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);
        List<ThreadSpecificData> specificDataList = new ArrayList<>(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++)
            specificDataList.add(new ThreadSpecificData(i, barrier,lock, input, output, filter.size(), NUM_THREADS, filter));

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

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
        assertEquals(result, output);

//        for (int i = 1; i < output.height - 1; i++) {
//            for (int j = 1; j < output.width - 1; j++) {
//                if(!output.matrix[i][j].equals(result.matrix[i][j]) ) {
//                    log.debug(String.format("diff at [%d][%d], out = %s, res = %s"
//                            , i
//                            , j
//                            , output.matrix[i][j]
//                            , result.matrix[i][j]));
//                    return;
//                }
//            }
//
//        }
    }

}
