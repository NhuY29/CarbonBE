package com.example.KLTN.projectManagement;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Enum.Category;
import com.example.KLTN.MeasurementData.MeasurementDataEntity;
import com.example.KLTN.signature.signatureEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "project_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_description")
    private String projectDescription;

    @Column(name = "project_status")
    private String projectStatus;

    @Column(name = "project_start_date")
    private String projectStartDate;

    @Column(name = "project_end_date")
    private String projectEndDate;

    @Column(name = "project_code")
    private String projectCode;

    @Column(name = "type")
    private String type;

    @Column(name = "standard")
    private String standard;

    @Column(name = "field")
    private String field;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private List<CoordinateEntity> coordinates;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImageEntity> images;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeasurementDataEntity> measurementData;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private signatureEntity signature;
}
