package com.example.KLTN.MeasurementData;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface MeasurementDataService {
    MeasurementDataEntity addMeasurementData(MeasurementDataEntity measurementDataEntity);
    List<MeasurementDetailsDTO> getMeasurementDetailsByMeasurementDataId(UUID measurementDataId);
    List<MeasurementDataDTO> getMeasurementDataByProjectId(UUID projectId);
    List<MeasurementDetailsDTO> getMeasurementDetailsByProjectId(UUID projectId);
    void deleteMeasurementData(UUID id);
    MeasurementDataEntity updateMeasurementData(UUID id, MeasurementDataRequest measurementDataRequest);
    MeasurementDataRequest getById(UUID id);
}
