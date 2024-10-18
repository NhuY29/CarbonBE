package com.example.KLTN.signature;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface signatureService {
    void saveSignature(UUID projectId, String signatureDataUrl, int numberOfProposals);
    String getSignature(UUID projectId);
    int getNumberOfProposals(UUID projectId);
    String getDocumentNumber(UUID projectId);
}
