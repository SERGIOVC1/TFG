package com.tfg.tfg.controllers;

import com.tfg.tfg.dto.WhoisLogRequest;
import com.tfg.tfg.persistance.model.WhoisLog;
import com.tfg.tfg.persistance.repository.WhoisLogRepository;
import com.tfg.tfg.services.WhoisService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.Instant;

@RestController
@RequestMapping("/api/whois")
@CrossOrigin(origins = "*")
public class WhoisController {

    @Autowired
    private WhoisService whoisService;

    @Autowired
    private WhoisLogRepository whoisLogRepository;

    @GetMapping
    public String getWhois(@RequestParam String domain) {
        return whoisService.lookup(domain);
    }

    @PostMapping("/log")
    public ResponseEntity<?> saveLog(@RequestBody WhoisLogRequest request) {
        try {
            String internalIp = InetAddress.getLocalHost().getHostAddress();

            WhoisLog log = new WhoisLog();
            log.setIpAddress(request.getIpAddress());
            log.setInternalIpAddress(internalIp);
            log.setDomain(request.getDomain());
            log.setWhoisResult(request.getWhoisResult());
            log.setToolUsed(request.getToolUsed());

            if (request.getTimestamp() != null) {
                log.setTimestamp(Instant.ofEpochMilli(request.getTimestamp()));
            } else {
                log.setTimestamp(Instant.now());
            }

            log.setUserAgent(request.getUserAgent());
            log.setBot(request.isBot());
            log.setLocation(request.getLocation());
            log.setAction(request.getAction());
            log.setDetails(request.getDetails());

            whoisLogRepository.save(log);
            return ResponseEntity.ok("Log WHOIS guardado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al registrar WHOIS: " + e.getMessage());
        }
    }

}
