package com.example.KLTN.projectManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequest {
    private String projectName;
    private String projectDescription;
    private String projectStatus;
    private String projectStartDate;
    private String projectEndDate;
    private String projectCode;
    private String type;
    private String standard;
    private  String field;
    private List<CoordinateEntity> coordinates;
    private String commune;
    private String district;
    private String conscious;
    private String city;
    private String aim;
}
