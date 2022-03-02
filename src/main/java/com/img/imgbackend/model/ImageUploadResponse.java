package com.img.imgbackend.model;

import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Value
public class ImageUploadResponse {
    String result = "OK";
    String id;
    Integer numberOfImages = 1;
    String message = "upload successful";

    public ImageUploadResponse(String id) {
        this.id = id;
    }
}
