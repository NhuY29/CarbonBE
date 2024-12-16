package com.example.KLTN.SampleSent;

import com.example.KLTN.Configuration.MailService;
import com.example.KLTN.Enum.Status;
import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class SampleSentImpl implements SampleSentService{
    @Autowired
    private SampleSentRepository sampleSentRepository;
    @Autowired
    private ProjectReponsitory projectRepository;
    @Autowired
    private MailService mailService;

    @Override
    public void savePdf(UUID projectId, MultipartFile file) {
        SampleSentEntity sampleSentEntity = new SampleSentEntity();
        try {
            byte[] pdfBytes = file.getBytes();
            System.out.println("Kích thước file PDF: " + pdfBytes.length);

            sampleSentEntity.setPdfFile(pdfBytes);
            sampleSentEntity.setProjectId(projectId);
            sampleSentEntity.setSendDate(LocalDateTime.now());
            sampleSentEntity.setStatus(Status.CHOXULY);
            sampleSentRepository.save(sampleSentEntity);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu tệp PDF: " + e.getMessage(), e);
        }
    }
    @Override
    public Optional<byte[]> getPdfByProjectId(UUID projectId, UUID id) {
        List<SampleSentEntity> sampleSentEntities = sampleSentRepository.findByProjectIdAndId(projectId, id);
        if (sampleSentEntities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sampleSentEntities.get(0).getPdfFile());
    }

    @Override
    public List<Map<String, Object>> getAllProjectIdsWithSendDate() {
        List<SampleSentEntity> allSamples = sampleSentRepository.findAll();
        List<Map<String, Object>> projectDetails = new ArrayList<>();

        for (SampleSentEntity sample : allSamples) {
            Map<String, Object> projectDetail = new HashMap<>();
            projectDetail.put("id", sample.getId());
            projectDetail.put("projectId", sample.getProjectId());
            projectDetail.put("sendDate", sample.getSendDate());
            projectDetails.add(projectDetail);
        }

        return projectDetails;
    }

    @Override
    public void savePdfReceived(UUID projectId, UUID id, MultipartFile file, int quantity) {
        List<SampleSentEntity> sampleSentEntities = sampleSentRepository.findByProjectIdAndId(projectId, id);
        if (!sampleSentEntities.isEmpty()) {
            SampleSentEntity sampleSentEntity = sampleSentEntities.get(0);
            if (file != null && !file.isEmpty()) {
                try {
                    byte[] pdfBytes = file.getBytes();
                    sampleSentEntity.setPdfFileReceived(pdfBytes);
                    sampleSentEntity.setSendDate(LocalDateTime.now());
                    sampleSentEntity.setQuantity((float) quantity);
                    sampleSentEntity.setStatus(Status.DAXULY);
                    sampleSentRepository.save(sampleSentEntity);
                } catch (IOException e) {
                    throw new RuntimeException("Không thể lưu tệp PDF nhận lại: " + e.getMessage(), e);
                }
            }
        } else {
            throw new RuntimeException("Không tìm thấy Project ID và ID tương ứng: " + projectId + ", " + id);
        }
    }


    public Optional<byte[]> getPdfReceivedByProjectId(UUID projectId, UUID id) {

        List<SampleSentEntity> sampleSentEntities = sampleSentRepository.findByProjectId(projectId);


        return sampleSentEntities.stream()
                .filter(sample -> sample.getId().equals(id))
                .findFirst()
                .map(SampleSentEntity::getPdfFileReceived);
    }

        @Override
        public List<SampleSentDTO> getAllProjectsWithPdfFileReceivedNull() {
            List<SampleSentEntity> sampleSentEntities = sampleSentRepository
                    .findByStatus(Status.CHOXULY);

            List<SampleSentDTO> projects = new ArrayList<>();

            for (SampleSentEntity sample : sampleSentEntities) {
                projects.add(new SampleSentDTO(
                        sample.getId(),
                        sample.getProjectId(),
                        sample.getSendDate(),
                        sample.getQuantity(),
                        sample.getReason()));
            }

            return projects;
        }

        @Override
        public List<SampleSentDTO> getAllProjectsWithPdfFileReceived() {
            List<SampleSentEntity> sampleSentEntities = sampleSentRepository
                    .findByStatus(Status.DAXULY);

            List<SampleSentDTO> projects = new ArrayList<>();

            for (SampleSentEntity sample : sampleSentEntities) {
                projects.add(new SampleSentDTO(
                        sample.getId(),
                        sample.getProjectId(),
                        sample.getSendDate(),
                        sample.getQuantity(),
                        sample.getReason()));
            }

            return projects;
        }
    @Override
    public List<SampleSentDTO> getAllProjectsWithStatusDaTuChoi() {
        // Tìm tất cả các SampleSentEntity có trạng thái DATUCHOI
        List<SampleSentEntity> sampleSentEntities = sampleSentRepository.findByStatus(Status.DATUCHOI);
        List<SampleSentDTO> projects = new ArrayList<>();

        // Chuyển đổi danh sách SampleSentEntity thành SampleSentDTO
        for (SampleSentEntity sample : sampleSentEntities) {
            projects.add(new SampleSentDTO(
                    sample.getId(),
                    sample.getProjectId(),
                    sample.getSendDate(),
                    sample.getQuantity(),
                    sample.getReason()
            ));
        }

        return projects;
    }


    @Override
    public List<SampleSentDTO> getProjectsWithSendDateToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<SampleSentEntity> sampleSentEntities = sampleSentRepository.findBySendDateBetween(startOfDay, endOfDay);
        List<SampleSentDTO> projects = new ArrayList<>();
        for (SampleSentEntity sample : sampleSentEntities) {

            projects.add(new SampleSentDTO(sample.getId(), sample.getProjectId(), sample.getSendDate(), sample.getQuantity(), sample.getReason()));
        }
        return projects;
    }
    @Override
    public void updateStatusToDaTuChoi(UUID projectId, String rejectionReason) {
        List<SampleSentEntity> sampleSentEntities = sampleSentRepository.findByProjectId(projectId);

        if (!sampleSentEntities.isEmpty()) {
            for (SampleSentEntity sampleSentEntity : sampleSentEntities) {
                sampleSentEntity.setStatus(Status.DATUCHOI);
                sampleSentEntity.setReason(rejectionReason);
                sampleSentRepository.save(sampleSentEntity);
            }

            Optional<ProjectEntity> projectEntityOpt = projectRepository.findById(projectId);
            if (projectEntityOpt.isPresent()) {
                ProjectEntity projectEntity = projectEntityOpt.get();
                projectEntity.setProjectStatus("Bị từ chối");
                projectRepository.save(projectEntity);

                // Lấy thông tin dự án để gửi vào email
                String recipientEmail = projectEntity.getUser().getUsername(); // Giả sử bạn có email liên hệ trong projectEntity
                String projectName = projectEntity.getProjectName();
                String projectCode = projectEntity.getProjectCode();  // Nếu cần mã dự án

                try {
                    // Gửi email thông báo lý do từ chối cùng thông tin dự án
                    mailService.sendRejectionEmail(recipientEmail, rejectionReason, projectName, projectCode);
                } catch (MessagingException e) {
                    // Xử lý lỗi khi gửi email
                    throw new RuntimeException("Lỗi khi gửi email thông báo: " + e.getMessage(), e);
                }
            } else {
                throw new RuntimeException("Không tìm thấy dự án với Project ID: " + projectId);
            }
        } else {
            throw new RuntimeException("Không tìm thấy SampleSentEntity cho Project ID: " + projectId);
        }
    }

}
