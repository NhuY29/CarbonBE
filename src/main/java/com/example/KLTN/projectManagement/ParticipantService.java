package com.example.KLTN.projectManagement;

import com.example.KLTN.Entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface ParticipantService {
    boolean joinProject(String token, UUID projectId);
    List<UserEntity> getParticipantsByProjectId(UUID projectId);
    boolean checkProjectParticipation(String token, UUID projectId);
}
