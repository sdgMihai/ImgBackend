package com.img.imgbackend.controller;

import com.img.imgbackend.model.ImageUploadResponse;
import com.img.imgbackend.model.ImgBin;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.repository.ImageRepository;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping("/api/")
public class Controller {
    private static final Logger log = LogManager.getLogger(Controller.class);
    private static int imgID = 0;
    private final ImageRepository imageRepository;
    private final ImgSrv imgSrv;
    private final ImageFormatIO imageFormatIO;

    @Autowired
    public Controller(ImageRepository imageRepository, ImgSrv imgSrv, ImageFormatIO imageFormatIO) {
        this.imageRepository = imageRepository;
        this.imgSrv = imgSrv;
        this.imageFormatIO = imageFormatIO;
    }

    @PostMapping(value = "/uploadImage")
    public ResponseEntity<ImageUploadResponse> uploadImage(MultipartHttpServletRequest request) throws IOException {
        imgID++;
        Iterator<String> itr = request.getFileNames();
        MultipartFile file = null;
        ImgBin imgBin = null;

        if (itr.hasNext()) {
            file = request.getFile(itr.next());
            assert file != null;
            imgBin = new ImgBin(String.valueOf(imgID), new Binary(file.getBytes()));
            log.debug(imgBin.id());

            imageRepository.save(imgBin);
        }

        assert imgBin != null;
        ImageUploadResponse res = new ImageUploadResponse(imageRepository.findById(imgBin.id()).get().id());
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/downloadImage/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> downloadImage(@PathVariable Integer id) throws IOException {
        if (imageRepository.findById(id.toString()).isPresent()) {
            byte[] image = imageRepository.findById(id.toString()).get().binary().getData();
            assert (image.length != 0);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

            final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
            final Image res = imgSrv.process(input);
            final BufferedImage image1 = imageFormatIO.modelToBufferedImage(res);
            final byte[] bytes = imageFormatIO.bufferedToByteArray(image1);
            return ResponseEntity.ok(bytes);
        } else {
            return ResponseEntity.ok(null);
        }
    }

}
