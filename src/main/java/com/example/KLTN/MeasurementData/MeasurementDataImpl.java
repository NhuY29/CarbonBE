package com.example.KLTN.MeasurementData;

import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MeasurementDataImpl implements MeasurementDataService{

    private final MeasurementDataRepository measurementDataRepository;

    private final ProjectReponsitory projectRepository;
    @Autowired
    public MeasurementDataImpl(MeasurementDataRepository measurementDataRepository, ProjectReponsitory projectRepository) {
        this.measurementDataRepository = measurementDataRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public MeasurementDataEntity addMeasurementData(MeasurementDataEntity measurementDataEntity) {
        return measurementDataRepository.save(measurementDataEntity);
    }
    @Override
    public List<MeasurementDetailsDTO> getMeasurementDetailsByMeasurementDataId(UUID measurementDataId) {
        // Lấy MeasurementDataEntity dựa trên measurementDataId
        MeasurementDataEntity measurementData = measurementDataRepository.findById(measurementDataId)
                .orElseThrow(() -> new IllegalArgumentException("Measurement data not found"));

        // Lấy danh sách MeasurementDetails và chuyển đổi thành DTO
        List<MeasurementDetailsDTO> measurementDetailsDTOs = new ArrayList<>();
        for (MeasurementDetailsEntity details : measurementData.getMeasurementDetails()) {
            MeasurementDetailsDTO dto = new MeasurementDetailsDTO(details.getWasteSource(), details.getGas(), details.getData());
            measurementDetailsDTOs.add(dto);
        }

        return measurementDetailsDTOs;
    }
    @Override
    public List<MeasurementDataDTO> getMeasurementDataByProjectId(UUID projectId) {
        // Kiểm tra xem project có tồn tại không
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Lấy tất cả dữ liệu đo lường thuộc về project này
        List<MeasurementDataEntity> measurementDataEntities = measurementDataRepository.findByProject_ProjectId(projectId);

        // Chuyển đổi danh sách MeasurementDataEntity sang MeasurementDataDTO
        List<MeasurementDataDTO> measurementDataDTOs = new ArrayList<>();
        for (MeasurementDataEntity entity : measurementDataEntities) {
            MeasurementDataDTO dto = new MeasurementDataDTO(
                    entity.getId(),
                    entity.getMeasurer(),
                    entity.getMeasurementDate(),
                    entity.getFarmingProcess(),
                    entity.getPhonelandowner(),
                    entity.getNamelandowner()
            );
            measurementDataDTOs.add(dto);
        }

        return measurementDataDTOs;
    }


}
