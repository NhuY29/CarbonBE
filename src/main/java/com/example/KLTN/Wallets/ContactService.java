package com.example.KLTN.Wallets;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface ContactService {
    ResponseEntity<?> saveContact(String secretKey, ContactEntity contact);

    void deleteContact(UUID id);

    ContactEntity updateContact(UUID id, ContactEntity contact);

    List<ContactEntity> searchContacts(String username);

    List<ContactEntity> getAllContacts();
    List<ContactDTO> getContactsByWalletSecretKey(String secretKey);
}
