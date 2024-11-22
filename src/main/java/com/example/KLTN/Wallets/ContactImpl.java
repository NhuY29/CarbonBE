package com.example.KLTN.Wallets;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContactImpl implements ContactService {
    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private SolanaReponsitory walletRepository;
    public ResponseEntity<?> saveContact(String secretKey, ContactEntity contact) {
        SolanaEntity wallet = walletRepository.findBySecretKey(secretKey)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        contact.setWallet(wallet);
        contactRepository.save(contact);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Contact created successfully"
        ));
    }



    public void deleteContact(UUID id) {
        if (!contactRepository.existsById(id)) {
            throw new RuntimeException("Liên hệ không tồn tại.");
        }
        contactRepository.deleteById(id);
    }


    public ContactEntity updateContact(UUID id, ContactEntity contact) {
        contact.setContactId(id);
        return contactRepository.save(contact);
    }

    public List<ContactEntity> searchContacts(String username) {
        return contactRepository.findByUsernameContaining(username);
    }

    public List<ContactEntity> getAllContacts() {
        return contactRepository.findAll();
    }

    public List<ContactDTO> getContactsByWalletSecretKey(String secretKey) {

        SolanaEntity wallet = walletRepository.findBySecretKey(secretKey)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        List<ContactEntity> contacts = contactRepository.findByWallet_WalletsId(wallet.getWalletsId());

        return contacts.stream()
                .map(contact -> new ContactDTO(contact.getContactId(),contact.getUsername(), contact.getWalletAddress()))
                .collect(Collectors.toList());
    }


}
