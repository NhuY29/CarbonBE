package com.example.KLTN.projectManagement;
import com.example.KLTN.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectReponsitory extends JpaRepository<ProjectEntity, UUID> {


    List<ProjectEntity> findByUser_UserId(UUID userId);
    @Query("SELECT p.user.userId FROM ProjectEntity p WHERE p.projectId = :projectId")
    Optional<UUID> findUserIdByProjectId(@Param("projectId") UUID projectId);

    List<ProjectEntity> findAllByProjectIdIn(List<UUID> projectIds);
}
