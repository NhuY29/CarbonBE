package com.example.KLTN.MeasurementData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementDataDTO {
    private UUID id;
    private String measurer;
    private String measurementDate;
    private String farmingProcess;
    private String phonelandowner;
    private String namelandowner;
}
