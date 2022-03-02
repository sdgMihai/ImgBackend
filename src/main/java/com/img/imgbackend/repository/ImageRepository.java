package com.img.imgbackend.repository;

import com.img.imgbackend.model.ImgBin;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageRepository extends MongoRepository<ImgBin, String> {

}
