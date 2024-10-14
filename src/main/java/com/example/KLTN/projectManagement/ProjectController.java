package com.example.KLTN.projectManagement;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.ResponseMessage;
import com.example.KLTN.Reponsitory.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;


    @Autowired
    public ProjectController(ProjectService projectService, ImageRepository imageRepository, UserRepository userRepository) {
        this.projectService = projectService;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;

    }
    private final String uploadDir = "D:\\ThucTapIT5\\MyFile\\";
    @GetMapping("/download/{projectId}")
    public ResponseEntity<Resource> downloadFiles(@PathVariable UUID projectId) {
        try {
            return projectService.downloadImagesByProjectId(projectId);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating ZIP file", e);
        }
    }
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable UUID projectId) {
        ProjectDTO project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }
    private final ObjectMapper objectMapper = new ObjectMapper();
    @PostMapping("/create")
    public ResponseEntity<ProjectEntity> createProject(
            @RequestParam String projectName,
            @RequestParam String projectDescription,
            @RequestParam String projectStatus,
            @RequestParam String projectStartDate,
            @RequestParam String projectEndDate,
            @RequestParam String projectCode,
            @RequestParam String type,
            @RequestParam String standard,
            @RequestParam String field,
            @RequestParam String coordinates,
            @RequestParam("images") List<MultipartFile> imageFiles) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String username = authentication.getName();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        UserEntity user = userOptional.get();

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setProjectName(projectName);
        projectRequest.setProjectDescription(projectDescription);
        projectRequest.setProjectStatus(projectStatus);
        projectRequest.setProjectStartDate(projectStartDate);
        projectRequest.setProjectEndDate(projectEndDate);
        projectRequest.setProjectCode(projectCode);
        projectRequest.setType(type);
        projectRequest.setStandard(standard);
        projectRequest.setField(field);

        try {
            List<CoordinateEntity> coordinatesList = objectMapper.readValue(coordinates,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CoordinateEntity.class));
            projectRequest.setCoordinates(coordinatesList);

            // Create ProjectEntity from ProjectRequest and associate with user
            ProjectEntity project = projectService.createProject(projectRequest, user);

            // Save images
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    String imageUrl = projectService.uploadImage(imageFile);

                    ImageEntity image = new ImageEntity();
                    image.setUrl(imageUrl);
                    image.setProject(project);
                    imageRepository.save(image);
                }
            }

            return new ResponseEntity<>(project, HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinates format");
        }
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDTO>> getProjectsByUser(@AuthenticationPrincipal Jwt jwt) {
        // Lấy username từ JWT
        String username = jwt.getClaimAsString("sub"); // Giả sử username nằm trong "sub"

        // Tìm UserEntity từ cơ sở dữ liệu dựa trên username
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Lấy danh sách các dự án của user
        List<ProjectDTO> projects = projectService.getProjectsByUserId(user.getUserId());
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/update/{projectId}")
    public ResponseEntity<ProjectEntity> updateProject(
            @PathVariable UUID projectId,
            @RequestParam String projectName,
            @RequestParam String projectDescription,
            @RequestParam String projectStatus,
            @RequestParam String projectStartDate,
            @RequestParam String projectEndDate,
            @RequestParam String projectCode,
            @RequestParam String type,
            @RequestParam String standard,
            @RequestParam String field, // Include field parameter
            @RequestParam String coordinates,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles) throws IOException {

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setProjectName(projectName);
        projectRequest.setProjectDescription(projectDescription);
        projectRequest.setProjectStatus(projectStatus);
        projectRequest.setProjectStartDate(projectStartDate);
        projectRequest.setProjectEndDate(projectEndDate);
        projectRequest.setProjectCode(projectCode);
        projectRequest.setType(type);
        projectRequest.setStandard(standard);
        projectRequest.setField(field); // Set field attribute

        try {
            List<CoordinateEntity> coordinatesList = objectMapper.readValue(coordinates,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CoordinateEntity.class));
            projectRequest.setCoordinates(coordinatesList);

            ProjectEntity project = projectService.updateProject(projectId, projectRequest);

            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        String imageUrl = projectService.uploadImage(imageFile);

                        ImageEntity image = new ImageEntity();
                        image.setUrl(imageUrl);
                        image.setProject(project);
                        imageRepository.save(image);
                    }
                }
            }

            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinates format");
        }
    }



    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<ResponseMessage> deleteProject(@PathVariable UUID projectId) {
        try {
            projectService.deleteProject(projectId);
            ResponseMessage responseMessage = new ResponseMessage(
                    "Project deleted successfully with ID: " + projectId,
                    true
            );
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            ResponseMessage responseMessage = new ResponseMessage(
                    ex.getReason(),
                    false
            );
            return new ResponseEntity<>(responseMessage, ex.getStatusCode());
        }
    }
    @GetMapping("/list")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }
}
