package com.example.KLTN.projectManagement;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Seller.SellerDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public interface ProjectService {
    ProjectEntity createProject(ProjectRequest projectRequest, UserEntity user);
    ProjectDTO getProjectById(UUID projectId);
    void deleteProject(UUID projectId);
    List<ProjectDTO> getAllProjects();
    String uploadImage(MultipartFile file) throws IOException;
    ResponseEntity<Resource> downloadImagesByProjectId(UUID projectId) throws IOException;
    List<ImageEntity> getImagesByProjectId(UUID projectId);
    ProjectEntity updateProject(UUID projectId, ProjectRequest projectRequest);
     List<ProjectDTO> getProjectsByUserId(UUID userId) ;
    SellerDTO getSellerByProjectId(UUID projectId);
}
