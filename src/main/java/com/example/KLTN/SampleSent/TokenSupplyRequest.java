package com.example.KLTN.SampleSent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenSupplyRequest {
    private UUID projectId;
    private UUID id;
}
