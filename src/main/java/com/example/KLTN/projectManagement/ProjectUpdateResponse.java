package com.example.KLTN.projectManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUpdateResponse {
    private String projectName;
    private String projectDescription;
    private String projectStatus;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private String projectCode;
    private String type;
    private String standard;
    private List<ImageRequest> images;
    private List<CoordinateEntity> coordinates;
}
