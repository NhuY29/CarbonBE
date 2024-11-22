package com.example.KLTN.Trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trade2")
    public class Trade2Controller {
        private final Trade2Service trade2Service;

        @Autowired
        public Trade2Controller(Trade2Service trade2Service) {
            this.trade2Service = trade2Service;
        }

    @GetMapping("/user/{userId}/project/{projectId}")
    public List<Trade2DTO> getAllTradesByUserIdAndProjectId(
            @PathVariable("userId") UUID userId,
            @PathVariable("projectId") UUID projectId) {

        return trade2Service.getAllByUserIdAndProjectId(userId, projectId);
    }
    @GetMapping("/user/{userId}/projects")
    public List<ProjectOfTrade2> getAllProjectsByUserId(@PathVariable("userId") UUID userId) {
        return trade2Service.getAllProjectsByUserId(userId);
    }

}
