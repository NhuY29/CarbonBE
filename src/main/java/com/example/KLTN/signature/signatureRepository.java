package com.example.KLTN.signature;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface signatureRepository extends JpaRepository<signatureEntity, UUID> {

    Optional<signatureEntity> findByProject_ProjectId(UUID projectId);
}
