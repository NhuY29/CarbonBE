package com.example.KLTN.projectManagement;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public interface ImageService {
    ResponseEntity<byte[]> getImageByUrlWithSize(String imageUrl, int width, int height);

}
