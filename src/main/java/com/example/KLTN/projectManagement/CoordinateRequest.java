package com.example.KLTN.projectManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateRequest {
    private double lat;
    private double lng;
    private double radius;
    private int order;
    private String type;
}
