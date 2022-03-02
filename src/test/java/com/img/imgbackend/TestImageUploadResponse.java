package com.img.imgbackend;

import com.img.imgbackend.model.ImageUploadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TestImageUploadResponse {
    @Test
    public void testID() {
        ImageUploadResponse imageUploadResponse = new ImageUploadResponse("1");
        assertEquals ("1", imageUploadResponse.getId());
    }

}
