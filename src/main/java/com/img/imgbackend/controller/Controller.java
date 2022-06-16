package com.img.imgbackend.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.img.imgbackend.filter.Filters;
import com.img.imgbackend.model.ImageUploadResponse;
import com.img.imgbackend.model.ImgBin;
import com.img.imgbackend.repository.ImageFormatIO;
import com.img.imgbackend.repository.ImageRepository;
import com.img.imgbackend.service.ImgSrv;
import com.img.imgbackend.utils.Image;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/")
public class Controller {
    private static int imgID = 0;
    private final ImageRepository imageRepository;
    private final ImgSrv imgSrv;
    private final ImageFormatIO imageFormatIO;
    private final RateLimiter r;

    @Autowired
    public Controller(ImageRepository imageRepository, ImgSrv imgSrv, ImageFormatIO imageFormatIO) {
        this.imageRepository = imageRepository;
        this.imgSrv = imgSrv;
        this.imageFormatIO = imageFormatIO;
        this.r = RateLimiter.create(3, 3, TimeUnit.SECONDS);
    }

    @PostMapping(value = "/filter", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> filterImage(MultipartHttpServletRequest request) throws IOException {
        r.acquire();
        Iterator<String> itr = request.getFileNames();
        MultipartFile file;

        if (itr.hasNext()) {
            file = request.getFile(itr.next());
            assert file != null;
            final byte[] image = file.getBytes();

            final String filter = request.getParameter("filter");
            List<String> argv = new java.util.ArrayList<>(List.of(filter));

            // filter is { brightness | contrast }
            if (filter.toLowerCase(Locale.ROOT)
                    .equals(Filters.BRIGHTNESS.toString().toLowerCase(Locale.ROOT))
                || filter.toLowerCase(Locale.ROOT)
                    .equals(Filters.CONTRAST.toString().toLowerCase(Locale.ROOT))) {
                argv.add(request.getParameter("level"));
                log.debug("level added: " + request.getParameter("level"));
                final double level = Double.parseDouble(request.getParameter("level"));
                log.debug("level added(double): " + argv.get(1));
            }

            assert (image.length != 0);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

            final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
            final Image res = imgSrv.process(input, argv);
            final BufferedImage image1 = imageFormatIO.modelToBufferedImage(res);
            final byte[] bytes = imageFormatIO.bufferedToByteArray(image1);
            return ResponseEntity.ok(bytes);
        }

        return null;
    }


    @PostMapping(value = "/uploadImage")
    public ResponseEntity<ImageUploadResponse> uploadImage(MultipartHttpServletRequest request) throws IOException {
        imgID++;
        Iterator<String> itr = request.getFileNames();
        MultipartFile file;
        ImgBin imgBin = null;

        if (itr.hasNext()) {
            file = request.getFile(itr.next());
            assert file != null;
            imgBin = new ImgBin(String.valueOf(imgID), new Binary(file.getBytes()));
            log.debug(imgBin.id());
            System.out.println(imgBin.id());

            imageRepository.save(imgBin);
        }

        final String filter = request.getParameter("filter");

        assert imgBin != null;

        final Optional<ImgBin> resImgBin = imageRepository.findById(imgBin.id());
        ImageUploadResponse res = null;
        if (resImgBin.isPresent())
            res = new ImageUploadResponse(resImgBin.get().id());
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/downloadImage/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> downloadImage(@PathVariable Integer id) throws IOException {
        if (imageRepository.findById(id.toString()).isPresent()) {
            byte[] image = imageRepository.findById(id.toString()).get().binary().getData();
            assert (image.length != 0);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

            final Image input = imageFormatIO.bufferedToModelImage(bufferedImage);
            final Image res = imgSrv.process(input, List.of("sepia"));
            final BufferedImage image1 = imageFormatIO.modelToBufferedImage(res);
            final byte[] bytes = imageFormatIO.bufferedToByteArray(image1);
            return ResponseEntity.ok(bytes);
        } else {
            return ResponseEntity.ok(null);
        }
    }

}
