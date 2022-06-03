package com.img.imgbackend;

import com.img.imgbackend.controller.WebConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(WebConfig.class)
@AutoConfigureWebMvc
@ContextConfiguration(classes = WebConfig.class)
@TestPropertySource(
        locations = "classpath:application.properties")
public class ImageRestControllerIntegrationTest {

    private static final Logger log = LogManager.getLogger(ImageRestControllerIntegrationTest.class);
    @Autowired
    private WebApplicationContext webAppContext;

    @Test
    public void upload() throws Exception {
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        log.debug(imageFile.toPath());
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        final MockPart image = new MockPart("Efficiency.png", "Efficiency.png", "image/gif", imageBytes);

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
        mvc.perform(
                        MockMvcRequestBuilders.multipart("/api/filter")
                                .file(image)
                                .param("filter", "black-white"))
                .andExpect(status().isOk());
    }
}
