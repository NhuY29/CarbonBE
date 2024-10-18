package com.example.KLTN.projectManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ImageImpl implements ImageService {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ProjectReponsitory projectRepository;

    private final String uploadDir = "D:\\ThucTapIT5\\MyFile\\";

    @Override
    public ResponseEntity<byte[]> getImageByUrlWithSize(String imageUrl, int width, int height) {
        // Load the image data
        byte[] imageData = loadImageData(imageUrl);
        if (imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // Resize the image
        byte[] resizedImageData = resizeImage(imageData, width, height);
        if (resizedImageData.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return ResponseEntity.ok().headers(headers).body(resizedImageData);
    }

    private byte[] loadImageData(String imageUrl) {
        try {
            Path imagePath = Paths.get(uploadDir).resolve(imageUrl);
            System.out.println("Attempting to access file at: " + imagePath.toString());

            if (!Files.exists(imagePath) || !Files.isReadable(imagePath)) {
                System.err.println("File not found or unreadable: " + imagePath.toString());
                return new byte[0];
            }
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private byte[] resizeImage(byte[] imageData, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImage == null) {
                System.err.println("Failed to read image data.");
                return new byte[0];
            }

            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, width, height, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            baos.flush();
            byte[] resizedImageData = baos.toByteArray();
            baos.close();

            return resizedImageData;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}

