package com.example.KLTN.MeasurementData;

import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("measurementData")
public class MeasurementDataController {
    private final MeasurementDataService measurementDataService;
    private final ProjectReponsitory projectRepository;

    @Autowired
    public MeasurementDataController(MeasurementDataService measurementDataService, ProjectReponsitory projectRepository) {
        this.measurementDataService = measurementDataService;
        this.projectRepository = projectRepository;
    }
    @PostMapping("/add")
    public ResponseEntity<List<MeasurementDataEntity>> addMeasurementData(
            @RequestBody MeasurementDataRequest measurementDataRequest) {

        ProjectEntity project = projectRepository.findById(measurementDataRequest.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        List<MeasurementDataEntity> createdMeasurements = new ArrayList<>();

        MeasurementDataEntity measurementDataEntity = new MeasurementDataEntity();
        measurementDataEntity.setMeasurer(measurementDataRequest.getMeasurer());
        measurementDataEntity.setMeasurementDate(measurementDataRequest.getMeasurementDate());
        measurementDataEntity.setFarmingProcess(measurementDataRequest.getFarmingProcess());
        measurementDataEntity.setPhonelandowner(measurementDataRequest.getPhonelandowner());
        measurementDataEntity.setNamelandowner(measurementDataRequest.getNamelandowner());
        measurementDataEntity.setProject(project);

        List<MeasurementDetailsEntity> measurementDetailsList = new ArrayList<>();
        for (MeasurementDataRequest.MeasurementDetails details : measurementDataRequest.getMeasurements()) {
            MeasurementDetailsEntity detailsEntity = new MeasurementDetailsEntity();
            detailsEntity.setWasteSource(details.getWasteSource());
            detailsEntity.setGas(details.getGas());
            detailsEntity.setData(details.getData());
            detailsEntity.setMeasurementData(measurementDataEntity);

            measurementDetailsList.add(detailsEntity);
        }

        measurementDataEntity.setMeasurementDetails(measurementDetailsList);
        MeasurementDataEntity createdData = measurementDataService.addMeasurementData(measurementDataEntity);
        createdMeasurements.add(createdData);

        return ResponseEntity.ok(createdMeasurements);
    }
    @GetMapping("/{measurementDataId}/details")
    public ResponseEntity<List<MeasurementDetailsDTO>> getMeasurementDetailsByMeasurementDataId(
            @PathVariable UUID measurementDataId) {

        // Gọi service để lấy danh sách MeasurementDetailsDTO dựa trên measurementDataId
        List<MeasurementDetailsDTO> measurementDetails = measurementDataService.getMeasurementDetailsByMeasurementDataId(measurementDataId);

        return ResponseEntity.ok(measurementDetails);
    }
    @GetMapping("/project/{projectId}")
    public List<MeasurementDataDTO> getMeasurementDataByProjectId(@PathVariable UUID projectId) {
        return measurementDataService.getMeasurementDataByProjectId(projectId);
    }




}
