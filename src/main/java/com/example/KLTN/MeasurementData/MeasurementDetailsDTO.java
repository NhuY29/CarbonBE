package com.example.KLTN.MeasurementData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementDetailsDTO {
    private String wasteSource;
    private String gas;
    private float data;

}
