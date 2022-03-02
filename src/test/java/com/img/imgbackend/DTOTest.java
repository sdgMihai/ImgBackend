package com.img.imgbackend;

import com.img.imgbackend.model.dto.ImageDTO;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DTOTest {
    @Test
    public void testDTO() throws IOException {
        File imageFile = new ClassPathResource("Efficiency.png").getFile();
        byte[] image = Files.readAllBytes(imageFile.toPath());

        ImageDTO dto = new ImageDTO(new Binary(image));
        assert (dto.binary() != null);
    }
}
