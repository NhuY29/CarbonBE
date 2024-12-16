package com.example.KLTN.projectManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coordinate")
public class CoordinateController {

    private final CoordinateService coordinateService;

    @Autowired
    public CoordinateController(CoordinateService coordinateService) {
        this.coordinateService = coordinateService;
    }

    @GetMapping("/active-coordinates")
    public List<CoordinateDTOAll> getActiveCoordinates() {
        return coordinateService.getActiveCoordinates();
    }
}
