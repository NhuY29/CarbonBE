package com.example.KLTN.SampleSent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SampleSentRepository extends JpaRepository<SampleSentEntity, UUID> {
    @Query("SELECT s FROM SampleSentEntity s WHERE s.projectId = :projectId")
    List<SampleSentEntity> findByProjectId(@Param("projectId") UUID projectId);

    List<SampleSentEntity> findByPdfFileReceivedIsNull();

    List<SampleSentEntity> findByPdfFileReceivedIsNotNull();

    List<SampleSentEntity> findBySendDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<SampleSentEntity> findByProjectIdAndId(UUID projectId, UUID id);

}
