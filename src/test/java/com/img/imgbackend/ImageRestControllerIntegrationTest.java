package com.img.imgbackend;

import com.img.imgbackend.model.ImgBin;
import com.img.imgbackend.repository.ImageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:application.properties")
public class ImageRestControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ImageRepository repository;

    private static final Logger log = LogManager.getLogger(ImageRestControllerIntegrationTest.class);

    @BeforeEach
    public void insertImage() throws Exception{
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        log.debug(imageFile.toPath());
        byte[] image = Files.readAllBytes(imageFile.toPath());
        ImgBin imgBinOne = new ImgBin("1", new Binary(image));
        Mockito.when(repository.findById("1"))
                .thenReturn(Optional.of(imgBinOne));
    }

    @Test
    public void givenOneImageExpectDownload()
            throws Exception {

        mvc.perform(post("/api/downloadImage/1")
                        .contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.IMAGE_PNG_VALUE));
    }

    @Test
    public void upload() throws Exception {
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        log.debug(imageFile.toPath());
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        final MockPart image = new MockPart("Efficiency.png", "Efficiency.png","image/gif", imageBytes);

        mvc.perform(
                fileUpload("/api/uploadImage").file(image)).andExpect(status().isOk());
    }
}
