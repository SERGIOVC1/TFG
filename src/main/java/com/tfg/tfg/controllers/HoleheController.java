package com.tfg.tfg.controllers;

import com.tfg.tfg.services.HoleheService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holehe")
@CrossOrigin(origins = "*")
public class HoleheController {

    private final HoleheService holeheService;

    public HoleheController(HoleheService holeheService) {
        this.holeheService = holeheService;
    }

    @GetMapping
    public String runHolehe(@RequestParam String email) {
        return holeheService.runHolehe(email);
    }
}
