package com.example.KLTN.SampleSent;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public interface SampleSentService {
    void savePdf(UUID projectId, MultipartFile file);
    Optional<byte[]> getPdfByProjectId(UUID projectId, UUID id);
    Optional<byte[]> getPdfReceivedByProjectId(UUID projectId, UUID id);
    List<Map<String, Object>> getAllProjectIdsWithSendDate();
    List<SampleSentDTO> getAllProjectsWithPdfFileReceivedNull();
    List<SampleSentDTO> getAllProjectsWithPdfFileReceived();
    List<SampleSentDTO> getProjectsWithSendDateToday();
    void savePdfReceived(UUID projectId, UUID id, MultipartFile file, int quantity);
    void updateStatusToDaTuChoi(UUID projectId, String rejectionReason);
    List<SampleSentDTO> getAllProjectsWithStatusDaTuChoi();
}
