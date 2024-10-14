package com.example.KLTN.projectManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/image")
public class ImageController {
    @Autowired
    private ImageService imageService;
    @Autowired
    private ProjectReponsitory projectRepository;
    private final String uploadDir = "D:/ThucTapIT5/MyFile/";

    @GetMapping("/get-images/{projectId}")
    public ResponseEntity<List<String>> getImagesByProjectId(
            @PathVariable UUID projectId) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isPresent()) {
            ProjectEntity project = projectOptional.get();
            List<String> imageUrls = project.getImages().stream()
                    .map(image -> image.getUrl())
                    .collect(Collectors.toList());

            if (imageUrls.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(imageUrls);
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping("/get-by-url")
    public ResponseEntity<byte[]> getImageByUrl(
            @RequestParam String imageUrl,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height) {
        return imageService.getImageByUrlWithSize(imageUrl, width != null ? width : 100, height != null ? height : 100);
    }
}


