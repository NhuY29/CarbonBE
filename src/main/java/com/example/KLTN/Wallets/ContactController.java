package com.example.KLTN.Wallets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/contacts")
public class ContactController {
    @Autowired
    private ContactService contactService;

    @PostMapping("/{secretKey}")
    public ResponseEntity<?> saveContact(@PathVariable String secretKey, @RequestBody ContactEntity contact) {
        return contactService.saveContact(secretKey, contact);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteContact(@PathVariable UUID id) {
        Map<String, String> response = new HashMap<>();
        try {
            contactService.deleteContact(id);
            response.put("message", "Xóa liên hệ thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi khi xóa liên hệ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<ContactEntity> updateContact(@PathVariable UUID id, @RequestBody ContactEntity contact) {
        return ResponseEntity.ok(contactService.updateContact(id, contact));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContactEntity>> searchContacts(@RequestParam String username) {
        return ResponseEntity.ok(contactService.searchContacts(username));
    }

    @GetMapping
    public ResponseEntity<List<ContactEntity>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }


    @GetMapping("/wallet/secret/{secretKey}")
    public ResponseEntity<List<ContactDTO>> getContactsByWalletSecretKey(@PathVariable String secretKey) {
        return ResponseEntity.ok(contactService.getContactsByWalletSecretKey(secretKey));
    }


}
