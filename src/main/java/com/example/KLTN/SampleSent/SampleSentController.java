package com.example.KLTN.SampleSent;

import com.example.KLTN.Reponsitory.UserRepository;
import com.example.KLTN.Seller.SellerDTO;
import com.example.KLTN.Trade.TradeEntity;
import com.example.KLTN.Trade.TradeRepository;
import com.example.KLTN.Wallets.SolanaReponsitory;
import com.example.KLTN.Wallets.WalletService;
import com.example.KLTN.commonCategory.CommonCategoryDTO;
import com.example.KLTN.commonCategory.CommonCategoryService;
import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import com.example.KLTN.projectManagement.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.util.*;

@RestController
@RequestMapping("/sampleSent")
public class SampleSentController {
    @Autowired
    private SampleSentService sampleSentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SolanaReponsitory walletRepository;
    @Autowired
    private ProjectReponsitory projectRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private CommonCategoryService commonCategoryService;
    @PostMapping("/TokenSupply")
    public ResponseEntity<Map<String, Object>> getSecretKey(@RequestParam("projectId") UUID projectId,
                                                            @RequestParam("quantity") Float quantity,
                                                            @RequestParam("price") String price) {
        try {
            // Lấy userId từ projectId
            UUID userId = projectRepository.findUserIdByProjectId(projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project không tồn tại hoặc không có userId liên quan"));

            // Lấy secretKey từ userId
            String secretKey = walletRepository.findSecretKeyByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secret key không tồn tại cho user này"));

            // Tạo mintToken từ secretKey và quantity
            String mintToken = walletService.createToken(secretKey, quantity.intValue());

            // Lấy thông tin dự án từ projectId
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project không tồn tại"));

            // Lấy thông tin người bán từ projectId
            SellerDTO sellerDTO = projectService.getSellerByProjectId(projectId);

            // Tạo mới một TradeEntity
            TradeEntity tradeEntity = new TradeEntity();
            tradeEntity.setProject(project);
            tradeEntity.setQuantity(quantity.intValue());
            tradeEntity.setProjectName(project.getProjectName());
            tradeEntity.setField(project.getField());
            tradeEntity.setCompanyName(sellerDTO.getCompanyName());
            tradeEntity.setMintToken(mintToken);
            tradeEntity.setPrice(price);
            tradeEntity.setUserId(userId);
            // Lấy name cho typeId và standardId
            if (project.getType() != null) {
                UUID typeId = UUID.fromString(project.getType());
                CommonCategoryDTO typeCategory = commonCategoryService.getCategoryById(typeId);
                tradeEntity.setTypeId(typeId);
                tradeEntity.setTypeName(typeCategory.getName()); // Lưu typeName vào TradeEntity
            } else {
                tradeEntity.setTypeId(null);
                tradeEntity.setTypeName(null);
            }

            if (project.getStandard() != null) {
                UUID standardId = UUID.fromString(project.getStandard());
                CommonCategoryDTO standardCategory = commonCategoryService.getCategoryById(standardId);
                tradeEntity.setStandardId(standardId);
                tradeEntity.setStandardName(standardCategory.getName());
            } else {
                tradeEntity.setStandardId(null);
                tradeEntity.setStandardName(null);
            }
            tradeEntity.setProjectDescription(project.getProjectDescription());
            // Lưu giao dịch vào database
            tradeRepository.save(tradeEntity);

            // Tạo phản hồi trả về
            Map<String, Object> response = new HashMap<>();
            response.put("secretKey", secretKey);
            response.put("mintToken", mintToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi khi lấy secretKey: " + e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadPdf(@RequestParam("projectId") String projectId,
                                                         @RequestParam("file") MultipartFile file) {
        try {
            sampleSentService.savePdf(UUID.fromString(projectId), file);
            Map<String, String> response = new HashMap<>();
            response.put("message", "PDF đã được lưu thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi kzhi lưu PDF: " + e.getMessage()));
        }
    }
    @GetMapping("/getPdf")
    public ResponseEntity<byte[]> getPdf(@RequestParam("projectId") String projectId, @RequestParam("id") String id) {
        Optional<byte[]> pdfData = sampleSentService.getPdfByProjectId(UUID.fromString(projectId), UUID.fromString(id));

        if (pdfData.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "sample.pdf");
            return new ResponseEntity<>(pdfData.get(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/getAll")
    public ResponseEntity<List<Map<String, Object>>> getAllProjectIds() {
        List<Map<String, Object>> projectDetails = sampleSentService.getAllProjectIdsWithSendDate();
        if (!projectDetails.isEmpty()) {
            return ResponseEntity.ok(projectDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(projectDetails);
        }
    }


    @PostMapping("/uploadReceived")
    public ResponseEntity<Map<String, String>> uploadPdfReceived(
            @RequestParam("projectId") String projectId,
            @RequestParam("id") String id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("quantity") int quantity) {
        try {
            sampleSentService.savePdfReceived(UUID.fromString(projectId), UUID.fromString(id), file, quantity);
            Map<String, String> response = new HashMap<>();
            response.put("message", "PDF nhận lại đã được lưu thành công!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "ID không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi khi lưu PDF nhận lại: " + e.getMessage()));
        }
    }

    @GetMapping("/getPdfReceived")
    public ResponseEntity<byte[]> getPdfReceived(@RequestParam("projectId") String projectId,
                                                 @RequestParam("id") String id) {
        UUID projectUuid = UUID.fromString(projectId);
        UUID sampleId = UUID.fromString(id);

        Optional<byte[]> pdfDataReceived = sampleSentService.getPdfReceivedByProjectId(projectUuid, sampleId);

        if (pdfDataReceived.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "sample_received.pdf");
            return new ResponseEntity<>(pdfDataReceived.get(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/getProjectsWithoutPdfReceived")
    public ResponseEntity<List<SampleSentDTO>> getAllProjectsWithoutPdfReceived() {
        List<SampleSentDTO> projects = sampleSentService.getAllProjectsWithPdfFileReceivedNull();
        if (!projects.isEmpty()) {
            return ResponseEntity.ok(projects);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(projects);
        }
    }
    @GetMapping("/getProjectsWithPdfReceived")
    public ResponseEntity<List<SampleSentDTO>> getAllProjectsWithPdfReceived() {
        List<SampleSentDTO> projects = sampleSentService.getAllProjectsWithPdfFileReceived();
        if (!projects.isEmpty()) {
            return ResponseEntity.ok(projects);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(projects);
        }
    }
    @GetMapping("/getProjectsSentToday")
    public ResponseEntity<List<SampleSentDTO>> getProjectsSentToday() {
        List<SampleSentDTO> projects = sampleSentService.getProjectsWithSendDateToday();
        if (!projects.isEmpty()) {
            return ResponseEntity.ok(projects);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(projects);
        }
    }
}
