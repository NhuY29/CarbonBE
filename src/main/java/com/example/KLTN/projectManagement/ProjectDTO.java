package com.example.KLTN.projectManagement;

import com.example.KLTN.DTO.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private UUID projectId;
    private String projectName;
    private String projectDescription;
    private String projectStatus;
    private String projectStartDate;
    private String projectEndDate;
    private String projectCode;
    private String type;
    private String standard;
    private String field;
    private List<ImageDTO> images;
    private List<CoordinateDTO> coordinates;
    private UserDTO user;
    private Float quantityBurn;
    private Float quantityNoburn;
    private String commune;   // Thêm commune
    private String district;  // Thêm district
    private String conscious; // Thêm conscious
    private String city;      // Thêm city
    private String aim;       // Thêm aim

    public ProjectDTO(UUID projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }
}
