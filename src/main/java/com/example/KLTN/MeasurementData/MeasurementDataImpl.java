package com.example.KLTN.MeasurementData;

import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        MeasurementDataEntity measurementData = measurementDataRepository.findById(measurementDataId)
                .orElseThrow(() -> new IllegalArgumentException("Measurement data not found"));

        List<MeasurementDetailsDTO> measurementDetailsDTOs = new ArrayList<>();
        for (MeasurementDetailsEntity details : measurementData.getMeasurementDetails()) {
            MeasurementDetailsDTO dto = new MeasurementDetailsDTO(details.getWasteSource(), details.getGas(), details.getData());
            measurementDetailsDTOs.add(dto);
        }

        return measurementDetailsDTOs;
    }
    @Override
    public List<MeasurementDataDTO> getMeasurementDataByProjectId(UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        List<MeasurementDataEntity> measurementDataEntities = measurementDataRepository.findByProject_ProjectId(projectId);
        List<MeasurementDataDTO> measurementDataDTOs = new ArrayList<>();
        for (MeasurementDataEntity entity : measurementDataEntities) {
            MeasurementDataDTO dto = new MeasurementDataDTO(
                    entity.getId(),
                    entity.getMeasurer(),
                    entity.getMeasurementDate(),
                    entity.getFarmingProcess(),
                    entity.getPhonelandowner(),
                    entity.getNamelandowner(),
                    entity.getProject().getProjectId()
            );
            measurementDataDTOs.add(dto);
        }

        return measurementDataDTOs;
    }

    @Override
    public List<MeasurementDetailsDTO> getMeasurementDetailsByProjectId(UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        List<MeasurementDataEntity> measurementDataEntities = measurementDataRepository.findByProject_ProjectId(projectId);
        List<MeasurementDetailsDTO> measurementDetailsDTOs = new ArrayList<>();
        for (MeasurementDataEntity entity : measurementDataEntities) {
            for (MeasurementDetailsEntity details : entity.getMeasurementDetails()) {
                MeasurementDetailsDTO dto = new MeasurementDetailsDTO(details.getWasteSource(), details.getGas(), details.getData());
                measurementDetailsDTOs.add(dto);
            }
        }

        return measurementDetailsDTOs;
    }
    @Override
    public void deleteMeasurementData(UUID id) {
        MeasurementDataEntity measurementData = measurementDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Measurement data not found"));


        measurementDataRepository.delete(measurementData);
    }
    @Override
    public MeasurementDataEntity updateMeasurementData(UUID id, MeasurementDataRequest measurementDataRequest) {
        MeasurementDataEntity existingData = measurementDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dữ liệu đo không được tìm thấy"));

        ProjectEntity project = projectRepository.findById(measurementDataRequest.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Dự án không được tìm thấy"));

        existingData.setMeasurer(measurementDataRequest.getMeasurer());
        existingData.setMeasurementDate(measurementDataRequest.getMeasurementDate());
        existingData.setFarmingProcess(measurementDataRequest.getFarmingProcess());
        existingData.setPhonelandowner(measurementDataRequest.getPhonelandowner());
        existingData.setNamelandowner(measurementDataRequest.getNamelandowner());
        existingData.setProject(project);


        List<MeasurementDetailsEntity> existingDetails = existingData.getMeasurementDetails();
        existingDetails.clear();


        List<MeasurementDetailsEntity> updatedDetailsList = new ArrayList<>();
        for (MeasurementDataRequest.MeasurementDetails details : measurementDataRequest.getMeasurements()) {
            MeasurementDetailsEntity detailsEntity = new MeasurementDetailsEntity();
            detailsEntity.setWasteSource(details.getWasteSource());
            detailsEntity.setGas(details.getGas());
            detailsEntity.setData(details.getData());
            detailsEntity.setMeasurementData(existingData); // Liên kết với MeasurementDataEntity

            updatedDetailsList.add(detailsEntity);
        }


        existingDetails.addAll(updatedDetailsList);


        return measurementDataRepository.save(existingData);
    }

    @Override
    public MeasurementDataRequest getById(UUID id) {

        MeasurementDataEntity existingData = measurementDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Measurement data not found"));


        MeasurementDataRequest request = new MeasurementDataRequest();
        request.setMeasurer(existingData.getMeasurer());
        request.setMeasurementDate(existingData.getMeasurementDate());
        request.setFarmingProcess(existingData.getFarmingProcess());
        request.setPhonelandowner(existingData.getPhonelandowner());
        request.setNamelandowner(existingData.getNamelandowner());
        request.setProjectId(existingData.getProject().getProjectId());


        List<MeasurementDataRequest.MeasurementDetails> detailsList = existingData.getMeasurementDetails().stream()
                .map(detail -> {
                    MeasurementDataRequest.MeasurementDetails measurementDetails = new MeasurementDataRequest.MeasurementDetails();
                    measurementDetails.setWasteSource(detail.getWasteSource());
                    measurementDetails.setGas(detail.getGas());
                    measurementDetails.setData(detail.getData());
                    return measurementDetails;
                })
                .collect(Collectors.toList());

        request.setMeasurements(detailsList);

        return request;
    }


}
