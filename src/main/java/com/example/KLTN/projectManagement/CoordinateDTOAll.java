package com.example.KLTN.projectManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateDTOAll {
    private UUID projectId;
    private double lat;
    private double lng;
    private double radius;
    private int order;
    private String type;
}
