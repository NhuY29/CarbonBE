package com.example.KLTN.projectManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoordinateRepository extends JpaRepository<CoordinateEntity, UUID> {

    @Query("""
        SELECT c.projectId AS projectId, c.lat AS lat, c.lng AS lng, 
               c.radius AS radius, c.order AS coordOrder, c.type AS type 
        FROM CoordinateEntity c
        JOIN ProjectEntity p ON c.projectId = p.projectId
        WHERE p.projectStatus = 'Đang hoạt động'
    """)
    List<Object[]> findActiveProjectCoordinates();
}
