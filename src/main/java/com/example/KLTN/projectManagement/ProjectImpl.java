package com.example.KLTN.projectManagement;


import com.example.KLTN.DTO.UserDTO;
import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Seller.SellerDTO;
import com.example.KLTN.Seller.SellerEntity;
import com.example.KLTN.Seller.SellerReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ProjectImpl implements ProjectService {
    @Autowired
    private ProjectReponsitory projectRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private SellerReponsitory sellerRepository;
    private final String uploadDir = "D:/ThucTapIT5/MyFile/";
    public SellerDTO getSellerByProjectId(UUID projectId) {

        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project ID not found"));

        UserEntity user = projectEntity.getUser();


        SellerEntity seller = sellerRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found for this user"));


        return convertToSellerDTO(seller);
    }
    private SellerDTO convertToSellerDTO(SellerEntity sellerEntity) {
        SellerDTO sellerDTO = new SellerDTO();


        sellerDTO.setSellerId(sellerEntity.getSellerId());
        sellerDTO.setUserId(sellerEntity.getUser().getUserId());
        sellerDTO.setCompanyName(sellerEntity.getCompanyName());
        sellerDTO.setContactPerson(sellerEntity.getContactPerson());
        sellerDTO.setContactEmail(sellerEntity.getContactEmail());
        sellerDTO.setContactPhone(sellerEntity.getContactPhone());

        return sellerDTO;
    }
    @Override
    public List<CoordinateDTO> getCoordinatesByProjectId(UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project ID not found"));

        // Chuyển đổi danh sách tọa độ từ ProjectEntity sang danh sách CoordinateDTO
        return projectEntity.getCoordinates().stream()
                .map(coordinate -> new CoordinateDTO(
                        coordinate.getLat(),
                        coordinate.getLng(),
                        coordinate.getRadius(), // Giả sử bạn đã có getter cho trường này
                        coordinate.getOrder(),  // Giả sử bạn đã có getter cho trường này
                        coordinate.getType()    // Giả sử bạn đã có getter cho trường này
                ))
                .collect(Collectors.toList());
    }


    @Override
    public ProjectEntity createProject(ProjectRequest projectRequest, UserEntity user) {
        ProjectEntity project = new ProjectEntity();
        project.setProjectId(UUID.randomUUID());
        project.setProjectName(projectRequest.getProjectName());
        project.setProjectDescription(projectRequest.getProjectDescription());
        project.setProjectStatus(projectRequest.getProjectStatus());
        project.setProjectStartDate(projectRequest.getProjectStartDate());
        project.setProjectEndDate(projectRequest.getProjectEndDate());
        project.setProjectCode(projectRequest.getProjectCode());
        project.setType(projectRequest.getType());
        project.setStandard(projectRequest.getStandard());
        project.setField(projectRequest.getField());
        project.setUser(user);
        project.setCoordinates(projectRequest.getCoordinates());

        return projectRepository.save(project);
    }
    @Override
    public List<ProjectDTO> getProjectsByUserId(UUID userId) {
        List<ProjectEntity> projects = projectRepository.findByUser_UserId(userId);
        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ImageEntity> getImagesByProjectId(UUID projectId) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isPresent()) {
            return projectOptional.get().getImages();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
    }
    @Override
    public ResponseEntity<Resource> downloadImagesByProjectId(UUID projectId) throws IOException {
        List<ImageEntity> images = getImagesByProjectId(projectId);
        if (images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found for this project");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (ImageEntity image : images) {
                Path filePath = Paths.get(uploadDir).resolve(image.getUrl());
                try (InputStream is = Files.newInputStream(filePath)) {
                    ZipEntry zipEntry = new ZipEntry(image.getUrl());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"images.zip\"");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(bais));
    }
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            String fileName = file.getOriginalFilename();
            File fileToSave = new File(uploadDir + fileName);
            file.transferTo(fileToSave);

            return fileName;
        } catch (IOException e) {
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    @Override
    public ProjectEntity updateProject(UUID projectId, ProjectRequest projectRequest) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setProjectName(projectRequest.getProjectName());
        project.setProjectDescription(projectRequest.getProjectDescription());
        project.setProjectStatus(projectRequest.getProjectStatus());
        project.setProjectStartDate(projectRequest.getProjectStartDate());
        project.setProjectEndDate(projectRequest.getProjectEndDate());
        project.setProjectCode(projectRequest.getProjectCode());
        project.setType(projectRequest.getType());
        project.setStandard(projectRequest.getStandard());
        project.setCoordinates(projectRequest.getCoordinates());
        project.setField(projectRequest.getField());
        return projectRepository.save(project);
    }


    @Override
    public void deleteProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project ID not found");
        }
        projectRepository.deleteById(projectId);
    }

    public List<ProjectDTO> getAllProjects() {
        List<ProjectEntity> projects = projectRepository.findAll();
        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    private ProjectDTO convertToDTO(ProjectEntity projectEntity) {
        ProjectDTO dto = new ProjectDTO();
        dto.setProjectId(projectEntity.getProjectId());
        dto.setProjectName(projectEntity.getProjectName());
        dto.setProjectDescription(projectEntity.getProjectDescription());
        dto.setProjectStatus(projectEntity.getProjectStatus());
        dto.setProjectStartDate(projectEntity.getProjectStartDate());
        dto.setProjectEndDate(projectEntity.getProjectEndDate());
        dto.setProjectCode(projectEntity.getProjectCode());
        dto.setType(projectEntity.getType());
        dto.setStandard(projectEntity.getStandard());
        dto.setField(projectEntity.getField());

        List<ImageDTO> images = projectEntity.getImages().stream()
                .map(image -> new ImageDTO(image.getImageId(), image.getUrl()))
                .collect(Collectors.toList());
        dto.setImages(images);

        List<CoordinateDTO> coordinates = projectEntity.getCoordinates().stream()
                .map(coordinate -> new CoordinateDTO(coordinate.getLat(), coordinate.getLng()))
                .collect(Collectors.toList());
        dto.setCoordinates(coordinates);

        // Lấy thông tin người sở hữu
        UserEntity userEntity = projectEntity.getUser();
        if (userEntity != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(userEntity.getUsername());
            userDTO.setFirstname(userEntity.getFirstname());
            userDTO.setLastname(userEntity.getLastname());
            dto.setUser(userDTO);
        }

        return dto;
    }

    public ProjectDTO getProjectById(UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project ID not found"));
        return convertToDTO(projectEntity);
    }

}
