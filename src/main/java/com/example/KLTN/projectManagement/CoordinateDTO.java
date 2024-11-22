package com.example.KLTN.projectManagement;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter

@NoArgsConstructor
public class CoordinateDTO {
    private double lat;
    private double lng;

    private double radius;
    private int order;
    private String type;

    public CoordinateDTO(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public CoordinateDTO(double lat, double lng, double radius, int order, String type) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.order = order;
        this.type = type;
    }
}
