package com.example.KLTN.projectManagement;

import jakarta.persistence.Column;
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

}
