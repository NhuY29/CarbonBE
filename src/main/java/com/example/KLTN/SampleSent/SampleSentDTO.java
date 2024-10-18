package com.example.KLTN.SampleSent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SampleSentDTO {
    private UUID id;
    private UUID projectId;
    private LocalDateTime sendDate;
}
