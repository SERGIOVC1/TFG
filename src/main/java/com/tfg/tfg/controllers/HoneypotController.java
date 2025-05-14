package com.tfg.tfg.controllers;

import com.tfg.tfg.services.HoneypotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/honeypot")
public class HoneypotController {

    private final HoneypotService honeypotService;

    @Autowired
    public HoneypotController(HoneypotService honeypotService) {
        this.honeypotService = honeypotService;
    }

    @PostMapping("/log")
    public void logAction(@RequestBody HoneypotActionRequest request) {
        System.out.println("📥 Registro recibido desde herramienta: " + request.getToolUsed());

        String userAgent = request.getUserAgent();
        boolean isBot = checkIfBot(userAgent);

        honeypotService.logHoneypotAction(
            request.getIpAddress(),
            request.getInternalIpAddress(),
            request.getAction(),
            request.getDetails(),
            request.getResult(),
            request.getToolUsed(),
            request.getTimestamp(),
            userAgent,
            isBot,
            request.getLocation() // ✅ nuevo campo
        );
    }

    private boolean checkIfBot(String userAgent) {
        List<String> bots = Arrays.asList("bot", "spider", "crawl", "scraper", "googlebot", "bingbot");
        return bots.stream().anyMatch(bot -> userAgent.toLowerCase().contains(bot));
    }

    public static class HoneypotActionRequest {
        private String ipAddress;
        private String internalIpAddress;
        private String action;
        private String details;
        private String result;
        private String toolUsed;
        private Long timestamp;
        private String userAgent;
        private String location; // ✅ nuevo campo

        // Getters y setters
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getInternalIpAddress() { return internalIpAddress; }
        public void setInternalIpAddress(String internalIpAddress) { this.internalIpAddress = internalIpAddress; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getToolUsed() { return toolUsed; }
        public void setToolUsed(String toolUsed) { this.toolUsed = toolUsed; }

        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}
