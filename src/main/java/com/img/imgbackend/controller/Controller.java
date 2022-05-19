package com.img.imgbackend.controller;

import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(value = "/filter", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> filterImage(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> itr = request.getFileNames();
        MultipartFile file;

        if (itr.hasNext()) {
            file = request.getFile(itr.next());
            assert file != null;
            final byte[] image = file.getBytes();

            String[] filterNames = null;
            if (request.getParameter("filter") != null) {
               filterNames = request.getParameter("filter").split(",");
            }

            String[] filterParams = null;
            if (request.getParameter("level") != null) {
                filterParams = request.getParameter("level").split(",");
            }
            assert (image.length != 0);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

            final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
            final CompletableFuture<Image> res;
            res = imgSrv.process(input, filterNames, filterParams);
            BufferedImage image1 = null;
            try {
                image1 = imageFormatIO.modelToBufferedImage(res.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            final byte[] bytes = imageFormatIO.bufferedToByteArray(image1);
            return ResponseEntity.ok(bytes);
        }

        return null;
    }

}
