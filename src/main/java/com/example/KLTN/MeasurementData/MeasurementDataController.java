package com.example.KLTN.MeasurementData;

import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public ResponseEntity<Map<String, Object>> addMeasurementData(
            @RequestBody MeasurementDataRequest measurementDataRequest) {

        ProjectEntity project = projectRepository.findById(measurementDataRequest.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Map<String, Object> response = new HashMap<>();

        try {
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
            measurementDataService.addMeasurementData(measurementDataEntity);

            response.put("message", "Thêm dữ liệu thành công");
            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Thêm dữ liệu thất bại: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/{measurementDataId}/details")
    public ResponseEntity<List<MeasurementDetailsDTO>> getMeasurementDetailsByMeasurementDataId(
            @PathVariable UUID measurementDataId) {

        List<MeasurementDetailsDTO> measurementDetails = measurementDataService.getMeasurementDetailsByMeasurementDataId(measurementDataId);

        return ResponseEntity.ok(measurementDetails);
    }
    @GetMapping("/project/{projectId}")
    public List<MeasurementDataDTO> getMeasurementDataByProjectId(@PathVariable UUID projectId) {
        return measurementDataService.getMeasurementDataByProjectId(projectId);
    }
    @GetMapping("/project/{projectId}/details")
    public ResponseEntity<List<MeasurementDetailsDTO>> getMeasurementDetailsByProjectId(@PathVariable UUID projectId) {
        List<MeasurementDetailsDTO> measurementDetails = measurementDataService.getMeasurementDetailsByProjectId(projectId);
        return ResponseEntity.ok(measurementDetails);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeasurementData(@PathVariable UUID id) {
        measurementDataService.deleteMeasurementData(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateMeasurementData(
            @PathVariable UUID id, @RequestBody MeasurementDataRequest measurementDataRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            MeasurementDataEntity updatedData = measurementDataService.updateMeasurementData(id, measurementDataRequest);
            response.put("success", true);
            response.put("message", "Cập nhật dữ liệu thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi không mong muốn xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/measurementDataRequest/{id}")
    public ResponseEntity<MeasurementDataRequest> getMeasurementDataRequestById(@PathVariable UUID id) {
        MeasurementDataRequest measurementDataRequest = measurementDataService.getById(id);
        return ResponseEntity.ok(measurementDataRequest);
    }


}
