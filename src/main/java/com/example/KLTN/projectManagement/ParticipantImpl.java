package com.example.KLTN.projectManagement;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ParticipantImpl implements ParticipantService {

    @Autowired
    private ParticipantRepository projectParticipantRepository;

    @Autowired
    private UserRepository userRepository;
    @Override
    public List<ParticipantDTO> getParticipantsByProjectId(UUID projectId) {
        List<ProjectParticipantEntity> participants = projectParticipantRepository.findByProjectId(projectId);
        return participants.stream()
                .map(participant -> {
                    UserEntity user = userRepository.findById(participant.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return new ParticipantDTO(user.getUsername(), user.getFirstname(), user.getLastname(), user.getRoles());
                })
                .collect(Collectors.toList());
    }


    @Override
    public boolean joinProject(String token, UUID projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        boolean alreadyJoined = projectParticipantRepository.existsByProjectIdAndUserId(projectId, user.getUserId());
        if (alreadyJoined) {
            return true;
        }

        ProjectParticipantEntity participant = new ProjectParticipantEntity();
        participant.setProjectId(projectId);
        participant.setUserId(user.getUserId());
        projectParticipantRepository.save(participant);

        return false;
    }
    public boolean checkProjectParticipation(String token, UUID projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        return projectParticipantRepository.existsByProjectIdAndUserId(projectId, user.getUserId());
    }


}
