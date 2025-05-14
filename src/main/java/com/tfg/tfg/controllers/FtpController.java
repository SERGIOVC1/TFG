package com.tfg.tfg.controllers;

import com.tfg.tfg.services.FtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ftp")
public class FtpController {

    @Autowired
    private FtpService ftpService;

    @GetMapping("/scan")
    public List<String> scanFtp(@RequestParam String target) {
        return ftpService.analyzeFtp(target);
    }
}
