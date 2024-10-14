package com.example.KLTN.projectManagement;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coordinates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoordinateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "lat")
    private double lat;

    @Column(name = "lng")
    private double lng;

    @Column(name = "radius")
    private double radius;

    @Column(name = "coord_order")
    private int order;

    @Column(name = "type")
    private String type;
}
