package com.img.imgbackend.controller;

import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/")
public class Controller {
    private final ImgSrv imgSrv;
    private final ImageFormatIO imageFormatIO;

    @Autowired
    public Controller(ImgSrv imgSrv, ImageFormatIO imageFormatIO) {
        this.imgSrv = imgSrv;
        this.imageFormatIO = imageFormatIO;
    }

    @Async
    @PostMapping(value = "/filter", produces = MediaType.IMAGE_PNG_VALUE)
    public CompletableFuture<ResponseEntity<byte[]>> filterImage(MultipartHttpServletRequest request) {
        Iterator<String> itr = request.getFileNames();

        MultipartFile file;

        if (itr.hasNext()) {
            file = request.getFile(itr.next());
            assert file != null;
            final byte[] image;
            try {
                image = file.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(ResponseEntity.internalServerError()
                        .build());
            }

            String[] filterNames = null;
            if (request.getParameter("filter") != null) {
                filterNames = request.getParameter("filter").split(",");
            }

            String[] filterParams = null;
            if (request.getParameter("level") != null) {
                filterParams = request.getParameter("level").split(",");
            }
            assert (image.length != 0);
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
            } catch (IOException e) {
                e.printStackTrace();
                log.debug(e.getMessage());
                return CompletableFuture.completedFuture(ResponseEntity.internalServerError()
                        .build());
            }

            final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
            final CompletableFuture<Image> res;
            res = imgSrv.process(input, filterNames, filterParams);
            log.debug("future of res obtained");
            BufferedImage image1;
            try {
                image1 = imageFormatIO.modelToBufferedImage(res.get());
                log.debug("buffered result image obtained");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                log.debug(e.getMessage());
                return CompletableFuture.completedFuture(ResponseEntity.internalServerError()
                        .build());
            }
            final byte[] bytes;
            try {
                bytes = imageFormatIO.bufferedToByteArray(image1);
                log.debug("bytes result image obtained");
            } catch (IOException e) {
                e.printStackTrace();
                log.debug(e.getMessage());
                return CompletableFuture.completedFuture(ResponseEntity.internalServerError()
                        .build());
            }
            log.debug("ready result image");
            return CompletableFuture.completedFuture(ResponseEntity.ok(bytes));
        }

        return null;

    }

}
