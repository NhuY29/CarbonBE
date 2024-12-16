package com.example.KLTN.projectManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoordinateService {

    private final CoordinateRepository coordinateRepository;

    @Autowired
    public CoordinateService(CoordinateRepository coordinateRepository) {
        this.coordinateRepository = coordinateRepository;
    }

    public List<CoordinateDTOAll> getActiveCoordinates() {
        List<Object[]> result = coordinateRepository.findActiveProjectCoordinates();

        return result.stream()
                .filter(coordinate -> coordinate[0] != null) // Bảo vệ tránh null
                .map(coordinate -> {
                    UUID projectId = (UUID) coordinate[0];
                    double lat = (double) coordinate[1];
                    double lng = (double) coordinate[2];
                    double radius = (double) coordinate[3];
                    int order = (int) coordinate[4];
                    String type = (String) coordinate[5];

                    return new CoordinateDTOAll(projectId, lat, lng, radius, order, type);
                })
                .collect(Collectors.toList());
    }
}

