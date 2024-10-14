package com.example.KLTN.Seller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;
    @PostMapping("/create")
    public ResponseEntity<SellerDTO> createSeller(@RequestBody SellerDTO sellerDTO) {
        SellerDTO createdSeller = sellerService.createSeller(sellerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSeller);
    }
}
