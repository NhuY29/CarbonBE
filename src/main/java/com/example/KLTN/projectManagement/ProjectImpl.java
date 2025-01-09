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
import java.util.*;
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
    public List<ConsciousDTO> getConsciousCounts() {
        List<Object[]> results = projectRepository.findConsciousCounts();

        return results.stream()
                .map(result -> {
                    String district = (String) result[0]; // Giá trị district
                    String conscious = (String) result[1]; // Giá trị conscious
                    Long count = (Long) result[2]; // Số lượng dự án
                    int projectCount = (count != null && count > 0) ? count.intValue() : 0;

                    return new ConsciousDTO(district, conscious, projectCount);
                })
                .collect(Collectors.toList());
    }

    public List<ConsciousDTO> getConsciousProjects(String conscious) {
        // Truy vấn các dự án từ repository, đã lọc theo trạng thái
        List<Object[]> results = projectRepository.findConsciousProjects(conscious);

        return results.stream()
                .map(result -> {
                    String district = (String) result[0]; // Giá trị district
                    String consciousResult = (String) result[1]; // Giá trị conscious
                    Long count = (Long) result[2]; // Số lượng dự án
                    int projectCount = (count != null && count > 0) ? count.intValue() : 0;

                    return new ConsciousDTO(district, consciousResult, projectCount);
                })
                .collect(Collectors.toList());
    }


    public List<CommuneDistrictDTO> getCommuneDistrictProjectCounts() {
        List<Object[]> results = projectRepository.findCommuneDistrictProjectCounts();

        return results.stream()
                .map(result -> {
                    String commune = (String) result[0];
                    String district = (String) result[1];
                    Long count = (Long) result[2];
                    int projectCount = (count != null && count > 0) ? count.intValue() : 1;

                    return new CommuneDistrictDTO(commune, district, projectCount);
                })
                .collect(Collectors.toList());
    }


    public List<Echart> getProjectTypeData(String conscious) {
        // Lọc các dự án có trạng thái khác "Không hoạt động"
        List<ProjectEntity> projects = projectRepository.findAll().stream()
                .filter(project -> !"Không hoạt động".equals(project.getProjectStatus()))  // Lọc các dự án có trạng thái không phải "Không hoạt động"
                .filter(project -> conscious.equals(project.getConscious()))  // Lọc theo giá trị conscious
                .collect(Collectors.toList());

        // Tạo các Map để lưu trữ thông tin số lượng và tổng số lượng của từng loại dự án
        Map<String, Integer> typeCountMap = new HashMap<>();
        Map<String, Integer> typeRejectedCountMap = new HashMap<>();  // Map để lưu số lượng dự án bị từ chối
        Map<String, Float> typeQuantityBurnSumMap = new HashMap<>();
        Map<String, Float> typeQuantityNoburnSumMap = new HashMap<>();

        // Duyệt qua các dự án để tính toán số lượng và tổng số lượng "burn" và "noburn"
        for (ProjectEntity project : projects) {
            String type = project.getType();

            // Cập nhật số lượng dự án theo loại
            typeCountMap.put(type, typeCountMap.getOrDefault(type, 0) + 1);

            // Kiểm tra xem dự án có bị từ chối không, nếu có thì cập nhật số lượng bị từ chối
            if ("Bị từ chối".equals(project.getProjectStatus())) {
                typeRejectedCountMap.put(type, typeRejectedCountMap.getOrDefault(type, 0) + 1);
            }

            // Cộng tổng số lượng quantityBurn theo loại
            Float quantityBurn = project.getQuantityBurn();
            if (quantityBurn != null) {
                typeQuantityBurnSumMap.put(type, typeQuantityBurnSumMap.getOrDefault(type, 0f) + quantityBurn);
            }

            // Cộng tổng số lượng quantityNoburn theo loại
            Float quantityNoburn = project.getQuantityNoburn();
            if (quantityNoburn != null) {
                typeQuantityNoburnSumMap.put(type, typeQuantityNoburnSumMap.getOrDefault(type, 0f) + quantityNoburn);
            }
        }

        // Tạo danh sách Echart để trả về dữ liệu cho mỗi loại dự án
        List<Echart> projectTypeData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            String type = entry.getKey();
            Integer projectCount = entry.getValue();
            Integer rejectedCount = typeRejectedCountMap.getOrDefault(type, 0);  // Số lượng dự án bị từ chối
            Float totalQuantityBurn = typeQuantityBurnSumMap.getOrDefault(type, 0f);
            Float totalQuantityNoburn = typeQuantityNoburnSumMap.getOrDefault(type, 0f);

            // Định dạng số lượng thành chuỗi với 2 chữ số thập phân
            String additionalQuantity = String.format("%.2f", totalQuantityBurn);
            String emissionReduction = String.format("%.2f", totalQuantityNoburn);

            // Thêm thông tin vào danh sách Echart
            projectTypeData.add(new Echart(type, projectCount, additionalQuantity, emissionReduction, rejectedCount));
        }

        return projectTypeData;
    }


    public List<Echart> getProjectStandardData(String conscious) {
        // Lọc các dự án có trạng thái không phải "Không hoạt động" và có conscious trùng khớp
        List<ProjectEntity> projects = projectRepository.findAll().stream()
                .filter(project -> !"Không hoạt động".equals(project.getProjectStatus()))  // Lọc các dự án có trạng thái không phải "Không hoạt động"
                .filter(project -> conscious.equals(project.getConscious()))  // Lọc theo giá trị conscious
                .collect(Collectors.toList());

        // Tạo các Map để lưu trữ thông tin số lượng và tổng số lượng của từng tiêu chuẩn dự án
        Map<String, Integer> standardCountMap = new HashMap<>();
        Map<String, Integer> standardRejectedCountMap = new HashMap<>();  // Map để lưu số lượng dự án bị từ chối
        Map<String, Float> standardQuantityBurnSumMap = new HashMap<>();
        Map<String, Float> standardQuantityNoburnSumMap = new HashMap<>();

        // Duyệt qua các dự án để tính toán số lượng và tổng số lượng "burn" và "noburn"
        for (ProjectEntity project : projects) {
            String standard = project.getStandard();

            // Cập nhật số lượng dự án theo tiêu chuẩn
            standardCountMap.put(standard, standardCountMap.getOrDefault(standard, 0) + 1);

            // Kiểm tra xem dự án có bị từ chối không, nếu có thì cập nhật số lượng bị từ chối
            if ("Bị từ chối".equals(project.getProjectStatus())) {
                standardRejectedCountMap.put(standard, standardRejectedCountMap.getOrDefault(standard, 0) + 1);
            }

            // Cộng tổng số lượng quantityBurn theo tiêu chuẩn
            Float quantityBurn = project.getQuantityBurn();
            if (quantityBurn != null) {
                standardQuantityBurnSumMap.put(standard, standardQuantityBurnSumMap.getOrDefault(standard, 0f) + quantityBurn);
            }

            // Cộng tổng số lượng quantityNoburn theo tiêu chuẩn
            Float quantityNoburn = project.getQuantityNoburn();
            if (quantityNoburn != null) {
                standardQuantityNoburnSumMap.put(standard, standardQuantityNoburnSumMap.getOrDefault(standard, 0f) + quantityNoburn);
            }
        }

        // Tạo danh sách Echart để trả về dữ liệu cho mỗi tiêu chuẩn dự án
        List<Echart> projectStandardData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : standardCountMap.entrySet()) {
            String standard = entry.getKey();
            Integer projectCount = entry.getValue();
            Integer rejectedCount = standardRejectedCountMap.getOrDefault(standard, 0);  // Số lượng dự án bị từ chối
            Float totalQuantityBurn = standardQuantityBurnSumMap.getOrDefault(standard, 0f);
            Float totalQuantityNoburn = standardQuantityNoburnSumMap.getOrDefault(standard, 0f);

            // Định dạng số lượng thành chuỗi với 2 chữ số thập phân
            String additionalQuantity = String.format("%.2f", totalQuantityBurn);
            String emissionReduction = String.format("%.2f", totalQuantityNoburn);

            // Thêm thông tin vào danh sách Echart
            projectStandardData.add(new Echart(standard, projectCount, additionalQuantity, emissionReduction, rejectedCount));
        }

        return projectStandardData;
    }

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

        return projectEntity.getCoordinates().stream()
                .map(coordinate -> new CoordinateDTO(
                        coordinate.getLat(),
                        coordinate.getLng(),
                        coordinate.getRadius(),
                        coordinate.getOrder(),
                        coordinate.getType()
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

        // Xử lý các trường mới
        project.setCommune(projectRequest.getCommune());
        project.setDistrict(projectRequest.getDistrict());
        project.setConscious(projectRequest.getConscious());
        project.setCity(projectRequest.getCity());
        project.setAim(projectRequest.getAim());

        return projectRepository.save(project);
    }

    @Override
    public List<ProjectDTO> getProjectsByUserId(UUID userId) {
        List<ProjectEntity> projects = projectRepository.findByUser_UserId(userId);

        List<ProjectEntity> filteredProjects = projects.stream()
                .filter(project -> "Không hoạt động".equals(project.getProjectStatus()))
                .collect(Collectors.toList());

        return filteredProjects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    @Override
    public List<ProjectDTO> getProjectsByUserIdDeny(UUID userId) {
        List<ProjectEntity> projects = projectRepository.findByUser_UserId(userId);

        List<ProjectEntity> filteredProjects = projects.stream()
                .filter(project -> "Bị từ chối".equals(project.getProjectStatus()))
                .collect(Collectors.toList());

        return filteredProjects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getProjectsByUserIdWithQuantityBurn(UUID userId) {
        List<ProjectEntity> projects = projectRepository.findByUser_UserId(userId);

        List<ProjectEntity> filteredProjects = projects.stream()
                .filter(project -> "Đang hoạt động".equals(project.getProjectStatus()) && project.getQuantityBurn() != null)
                .collect(Collectors.toList());

        return filteredProjects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    public void updateQuantityBurn(UUID projectId, Float newQuantityBurn) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Tính toán lại quantityBurn, nếu quantityBurn hiện tại là null thì gán giá trị 0
        Float updatedQuantityBurn = (project.getQuantityBurn() != null ? project.getQuantityBurn() : 0) + newQuantityBurn;

        // Kiểm tra nếu updatedQuantityBurn > 0 thì gán về 0
        if (updatedQuantityBurn > 0) {
            updatedQuantityBurn = 0f;
        }

        // Cập nhật lại quantityBurn của dự án
        project.setQuantityBurn(updatedQuantityBurn);
        projectRepository.save(project);
    }



    @Override
    public List<ProjectDTO> getProjectsByUserIdWithQuantityNull(UUID userId) {
        List<ProjectEntity> projects = projectRepository.findByUser_UserId(userId);

        // Lọc các dự án có quantityNoburn != null và đang hoạt động
        List<ProjectEntity> filteredProjects = projects.stream()
                .filter(project -> "Đang hoạt động".equals(project.getProjectStatus()) && project.getQuantityNoburn() != null)
                .collect(Collectors.toList());

        return filteredProjects.stream().map(this::convertToDTO).collect(Collectors.toList());
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
        project.setCommune(projectRequest.getCommune());
        project.setDistrict(projectRequest.getDistrict());
        project.setConscious(projectRequest.getConscious());
        project.setCity(projectRequest.getCity());
        project.setAim(projectRequest.getAim());
        return projectRepository.save(project);
    }


    @Override
    public void deleteProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project ID not found");
        }
        projectRepository.deleteById(projectId);
    }

    public List<ProjectDTO> getActiveProjects() {
        // Lấy danh sách dự án có trạng thái "Đang hoạt động"
        List<ProjectEntity> activeProjects = projectRepository.findByProjectStatus("Đang hoạt động");

        // Chuyển đổi danh sách dự án từ ProjectEntity sang ProjectDTO bằng cách sử dụng convertToDTO()
        return activeProjects.stream()
                .map(this::convertToDTO) // Sử dụng phương thức convertToDTO đã định nghĩa
                .collect(Collectors.toList());
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
        dto.setQuantityBurn(projectEntity.getQuantityBurn());
        dto.setQuantityNoburn(projectEntity.getQuantityNoburn());

        dto.setCommune(projectEntity.getCommune());
        dto.setDistrict(projectEntity.getDistrict());
        dto.setConscious(projectEntity.getConscious());
        dto.setCity(projectEntity.getCity());
        dto.setAim(projectEntity.getAim());

        List<ImageDTO> images = projectEntity.getImages().stream()
                .map(image -> new ImageDTO(image.getImageId(), image.getUrl()))
                .collect(Collectors.toList());
        dto.setImages(images);

        List<CoordinateDTO> coordinates = projectEntity.getCoordinates().stream()
                .map(coordinate -> new CoordinateDTO(coordinate.getLat(), coordinate.getLng()))
                .collect(Collectors.toList());
        dto.setCoordinates(coordinates);

        UserEntity userEntity = projectEntity.getUser();
        if (userEntity != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserId(userEntity.getUserId());
            userDTO.setUsername(userEntity.getUsername());
            userDTO.setFirstname(userEntity.getFirstname());
            userDTO.setLastname(userEntity.getLastname());
            userDTO.setRoles(userEntity.getRoles());
            userDTO.setStatus(userEntity.isStatus());
            userDTO.setDelete(userEntity.isDelete());
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
