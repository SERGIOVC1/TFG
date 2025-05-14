package com.tfg.tfg.controllers;

import com.tfg.tfg.services.WhoisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whois")
@CrossOrigin(origins = "*") // Por si accedes desde frontend
public class WhoisController {

    @Autowired
    private WhoisService whoisService;

    @GetMapping
    public String getWhois(@RequestParam String domain) {
        return whoisService.lookup(domain);
    }
}
