package com.example.KLTN.MeasurementData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementDataRequest {
    private List<MeasurementDetails> measurements;
    private String measurer;
    private String measurementDate;
    private String farmingProcess;
    private String phonelandowner;
    private String namelandowner;
    private UUID projectId;

    @Getter
    @Setter
    public static class MeasurementDetails {
        private String wasteSource;
        private String gas;
        private float data;
    }
}
