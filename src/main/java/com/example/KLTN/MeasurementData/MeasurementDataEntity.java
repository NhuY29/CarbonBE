package com.example.KLTN.MeasurementData;

import com.example.KLTN.projectManagement.ProjectEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "measurement_data")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeasurementDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "measurer")
    private String measurer;

    @Column(name = "measurementDate")
    private String measurementDate;

    @Column(name = "farmingProcess")
    private String farmingProcess;

    @Column(name = "Phonelandowner")
    private String phonelandowner;

    @Column(name = "Namelandowner")
    private String namelandowner;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    private ProjectEntity project;


    @OneToMany(mappedBy = "measurementData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeasurementDetailsEntity> measurementDetails;
}
