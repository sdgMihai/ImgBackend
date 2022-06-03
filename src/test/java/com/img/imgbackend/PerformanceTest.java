package com.img.imgbackend;

import com.img.imgbackend.filter.tasks.TaskFactory;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
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
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Import(
        value = {
                ImageFormatIO.class
        }
)
@Slf4j
@Disabled
public class PerformanceTest {
    @Autowired
    private ImageFormatIO imageFormatIO;

    private static final int THRESHOLD = 1250;

    @RepeatedTest(1)
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
        String[] filterName = new String[]{"canny-edge-detection"};

        ForkJoinPool commonPool = ForkJoinPool.commonPool();

        List<RecursiveAction> filters = TaskFactory.getFilters(filterName, null, input, output, THRESHOLD);
        filters.forEach(commonPool::invoke);

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);
        assertEquals(result, output);

        for (int i = 1; i < output.height - 1; i++) {
            for (int j = 1; j < output.width - 1; j++) {
                if(!output.matrix[i][j].equals(result.matrix[i][j]) ) {
                    log.debug(String.format("diff at [%d][%d], out = %s, res = %s"
                            , i
                            , j
                            , output.matrix[i][j]
                            , result.matrix[i][j]));
                    assertEquals(output.matrix[i][j], result.matrix[i][j]);
                    return;
                }
            }

        }
    }

}
