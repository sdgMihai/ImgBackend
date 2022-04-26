package com.img.imgbackend;

import com.img.imgbackend.filter.BlackWhiteFilter;
import com.img.imgbackend.filter.GaussianBlurFilter;
import com.img.imgbackend.filter.tasks.GradientTask;
import com.img.imgbackend.filter.tasks.SingleOpTask;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.mongodb.assertions.Assertions.assertNotNull;
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
public class TestTask {
    private static Image input;
    private static final int THRESHOLD = 160;

    @Autowired
    private ImageFormatIO imageFormatIO;

    @BeforeAll
    public static void readInputAndResult(@Autowired ImageFormatIO imageFormatIO) throws IOException {
        // read input file
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        byte[] image = Files.readAllBytes(imageFile.toPath());
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        input = imageFormatIO.bufferedToModelImage(bufferedImage);

    }

    @Test
    public void testBWTask() throws IOException {
        final Image output = new Image(input.width - 2, input.height - 2);

        final SingleOpTask bwTask = new SingleOpTask(input
                , output
                , 1
                , input.height - 1
                , new BlackWhiteFilter()
                , THRESHOLD);
        bwTask.fork();
        bwTask.join();

        Arrays.stream(output.matrix).flatMap(Stream::of).forEach(
                pixel -> assertThat(pixel.r
                        , anyOf(is(pixel.g)
                                , is(pixel.b)
                                , is(pixel.a)
                        ))
        );

        // read bw task res file
        File outputFile = new ClassPathResource("efficiencyBW.png").getFile();  //efficiencyBW
        byte[] bwResponse = Files.readAllBytes(outputFile.toPath());
        BufferedImage bufferedBWResponse = ImageIO.read(new ByteArrayInputStream(bwResponse));
        Image bwImage = imageFormatIO.bufferedToModelImage(bufferedBWResponse);

        for (int i = 1; i < output.height - 1; i++) {
            for (int j = 1; j < output.width - 1; j++) {
                assertTrue((bwImage.matrix[i][j].r - output.matrix[i][j].r) <= 1
                        , String.format("This is more than floating point inexactitude at m[%d][%d]", i, j));
            }
        }
    }

    @Test
    public void testGBTask() throws IOException {
        final Image output = new Image(input.width - 2, input.height - 2);

        final SingleOpTask gbTask = new SingleOpTask(input
                , output
                , 1
                , input.height - 1
                , new GaussianBlurFilter()
                , THRESHOLD);
        gbTask.fork();
        gbTask.join();

        // read gb task res file
        File outputFile = new ClassPathResource("efficiencyGB.png").getFile();
        byte[] gbResponse = Files.readAllBytes(outputFile.toPath());
        BufferedImage bufferedGBResponse = ImageIO.read(new ByteArrayInputStream(gbResponse));
        Image gbImage = imageFormatIO.bufferedToModelImage(bufferedGBResponse);

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
    public void testGTask() throws IOException {
        final Image output = new Image(input.width - 2, input.height - 2);

        final GradientTask gTask = new GradientTask(input, output, 1, input.height - 1, THRESHOLD);
        gTask.fork();
        gTask.join();

        assertEquals(input.width, output.width);
        assertEquals(input.height, output.height);

        final BufferedImage image1 = imageFormatIO.modelToBufferedImage(output);
        final byte[] bytes = imageFormatIO.bufferedToByteArray(image1);

        ClassLoader classLoader = getClass().getClassLoader();
        File outputFile1 =  new File(classLoader.getResource(".").getFile() + "/efficiencyGTask.png");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile1)) {
            log.debug("wrote to file response");
            outputStream.write(bytes);
        }


        // read g task res file
        File outputFile = new ClassPathResource("efficiencyG.png").getFile();
        byte[] gResponse = Files.readAllBytes(outputFile.toPath());
        BufferedImage bufferedGResponse = ImageIO.read(new ByteArrayInputStream(gResponse));
        Image gImage = imageFormatIO.bufferedToModelImage(bufferedGResponse);

        for (int i = 1; i < output.height - 1; i++) {
            for (int j = 1; j < output.width - 1; j++) {
                assertNotNull(output.matrix[i][j]);
                assertTrue((gImage.matrix[i][j].r - output.matrix[i][j].r) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].r->%d != %d"
                                , i
                                , j
                                , (int)gImage.matrix[i][j].r
                                , (int)output.matrix[i][j].r));

                assertTrue((gImage.matrix[i][j].g - output.matrix[i][j].g) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].g", i, j));

                assertTrue((gImage.matrix[i][j].b - output.matrix[i][j].b) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].b", i, j));

                assertTrue((gImage.matrix[i][j].a - output.matrix[i][j].a) <= 1
                        , String.format("This is more than fp inexactitude at m[%d][%d].a", i, j));
            }
        }
    }


}
