package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.TracerouteLog;
import com.tfg.tfg.services.TracerouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traceroute")
@CrossOrigin(origins = "*")
public class TracerouteController {

    @Autowired
    private TracerouteService tracerouteService;

    @GetMapping
    public List<String> trace(
            @RequestParam String target,
            @RequestParam(required = false) String userId
    ) {
        return tracerouteService.executeTraceroute(target, userId);
    }

    @GetMapping("/logs")
    public List<TracerouteLog> getLogs(@RequestParam(required = false) String userId) {
        return tracerouteService.getLogsByUserId(userId);
    }
}