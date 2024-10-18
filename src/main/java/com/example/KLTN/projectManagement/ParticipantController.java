package com.example.KLTN.projectManagement;

import com.example.KLTN.Entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/participant")
public class ParticipantController {
    @Autowired
    private ParticipantService participantService;

    @PostMapping("/join")
    public ResponseEntity<Boolean> joinProject(
            @RequestHeader("Authorization") String token,
            @RequestParam("projectId") UUID projectId) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            boolean isJoined = participantService.joinProject(token, projectId);
            return new ResponseEntity<>(isJoined, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkProjectParticipation(
            @RequestHeader("Authorization") String token,
            @RequestParam("projectId") UUID projectId) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            boolean isJoined = participantService.checkProjectParticipation(token, projectId);
            return new ResponseEntity<>(isJoined, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ParticipantDTO>> getParticipantsByProjectId(
            @PathVariable UUID projectId) {
        List<ParticipantDTO> participants = participantService.getParticipantsByProjectId(projectId);
        return new ResponseEntity<>(participants, HttpStatus.OK);
    }


}
