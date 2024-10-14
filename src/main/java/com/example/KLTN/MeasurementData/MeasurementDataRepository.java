package com.example.KLTN.MeasurementData;

import com.example.KLTN.projectManagement.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeasurementDataRepository extends JpaRepository<MeasurementDataEntity, UUID> {

    List<MeasurementDataEntity> findByProject_ProjectId(UUID projectId);
}
