package com.example.KLTN.signature;

import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class signatureImpl implements signatureService {

    @Autowired
    private signatureRepository signatureRepository;

    @Autowired
    private ProjectReponsitory projectRepository;

    public String getSignature(UUID projectId) {
        signatureEntity signature = signatureRepository.findByProject_ProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Signature not found for project ID: " + projectId));
        return signature.getSignatureDataUrl();
    }

    public int getNumberOfProposals(UUID projectId) {
        signatureEntity signature = signatureRepository.findByProject_ProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Signature not found for project ID: " + projectId));
        return signature.getNumberOfProposals();
    }

    public String getDocumentNumber(UUID projectId) {
        signatureEntity signature = signatureRepository.findByProject_ProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Signature not found for project ID: " + projectId));
        return signature.getDocumentNumber();
    }

    public void saveSignature(UUID projectId, String signatureDataUrl, int numberOfProposals) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found for project ID: " + projectId));

        signatureEntity signature = signatureRepository.findByProject_ProjectId(projectId)
                .orElse(new signatureEntity());

        signature.setProject(project);
        signature.setSignatureDataUrl(signatureDataUrl);
        signature.setNumberOfProposals(numberOfProposals);

        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String documentNumber = dateTime + "-" + projectId.toString();
        signature.setDocumentNumber(documentNumber);

        signatureRepository.save(signature);
    }

}
