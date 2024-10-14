package com.example.KLTN.projectManagement;
import com.example.KLTN.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectReponsitory extends JpaRepository<ProjectEntity, UUID> {


    List<ProjectEntity> findByUser_UserId(UUID userId);
}
