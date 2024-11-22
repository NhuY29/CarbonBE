package com.example.KLTN.Trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class Trade2Impl implements Trade2Service{
    private final Trade2Repository trade2Repository;

    @Autowired
    public Trade2Impl(Trade2Repository trade2Repository) {
        this.trade2Repository = trade2Repository;
    }
    @Override
    public List<Trade2DTO> getAllByUserIdAndProjectId(UUID userId, UUID projectId) {
        List<Trade2Entity> trade2Entities = trade2Repository.findAllByUserIdAndProject_ProjectId(userId, projectId);
        List<Trade2DTO> tradeDTOs = trade2Entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        Collections.reverse(tradeDTOs);

        return tradeDTOs;
    }


    @Override
    public List<ProjectOfTrade2> getAllProjectsByUserId(UUID userId) {
        List<Trade2Entity> trade2Entities = trade2Repository.findAllByUserId(userId);

        return trade2Entities.stream()
                .map(trade -> new ProjectOfTrade2(
                        trade.getProject().getProjectName(),
                        trade.getMintToken(),
                        trade.getProject().getProjectId()
                ))
                .distinct()
                .collect(Collectors.toList());
    }


    public Trade2DTO convertToDTO(Trade2Entity trade2Entity) {
        return new Trade2DTO(

                trade2Entity.getMintToken()

        );
    }

}
