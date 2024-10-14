package com.example.KLTN.MeasurementData;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface MeasurementDataService {
    MeasurementDataEntity addMeasurementData(MeasurementDataEntity measurementDataEntity);
    List<MeasurementDetailsDTO> getMeasurementDetailsByMeasurementDataId(UUID measurementDataId);
    List<MeasurementDataDTO> getMeasurementDataByProjectId(UUID projectId);
}
