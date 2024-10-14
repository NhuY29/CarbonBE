package com.example.KLTN.MeasurementData;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "measurement_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeasurementDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "wasteSource")
    private String wasteSource;

    @Column(name = "gas")
    private String gas;

    @Column(name = "data")
    private float data;

    // ManyToOne relationship with MeasurementDataEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_data_id", nullable = false)
    private MeasurementDataEntity measurementData;
}
