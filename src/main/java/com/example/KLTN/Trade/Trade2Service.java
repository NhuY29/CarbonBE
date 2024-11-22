package com.example.KLTN.Trade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Trade2Service {
    List<Trade2DTO> getAllByUserIdAndProjectId(UUID userId, UUID projectId) ;
    List<ProjectOfTrade2> getAllProjectsByUserId(UUID userId);
}
