package com.example.KLTN.signature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResponse {
    private String signatureDataUrl;
    private int numberOfProposals;
    private String documentNumber;
}
