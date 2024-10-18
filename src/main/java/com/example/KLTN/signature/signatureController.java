package com.example.KLTN.signature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/signature")
public class signatureController {
    @Autowired
    private signatureService signatureService;
    @Autowired signatureRepository signatureRepository;

    @PostMapping("/save")
    public ResponseEntity<Void> saveSignature(@RequestParam UUID projectId,
                                              @RequestParam String signatureDataUrl,
                                              @RequestParam int numberOfProposals) {
        signatureService.saveSignature(projectId, signatureDataUrl, numberOfProposals);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/get")
    public ResponseEntity<SignatureResponse> getSignature(@RequestParam UUID projectId) {
        String signatureDataUrl = signatureService.getSignature(projectId);
        int numberOfProposals = signatureService.getNumberOfProposals(projectId);
        String documentNumber = signatureService.getDocumentNumber(projectId);

        SignatureResponse response = new SignatureResponse(signatureDataUrl, numberOfProposals, documentNumber);


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
