package com.tfg.tfg.services;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FtpService {

    public List<String> analyzeFtp(String target) {
        List<String> sensitiveFiles = new ArrayList<>();
        FTPClient ftpClient = new FTPClient();

        try {
            System.out.println("🔍 Conectando al servidor FTP: " + target);
            ftpClient.connect(target, 21);
            ftpClient.login("anonymous", "anonymous");
            ftpClient.enterLocalPassiveMode();

            FTPFile[] files = ftpClient.listFiles();

            if (files != null) {
                for (FTPFile file : files) {
                    String fileName = file.getName();
                    
                    // 🚨 Detecta archivos sensibles
                    if (fileName.toLowerCase().contains("config") || 
                        fileName.toLowerCase().contains("passwd") || 
                        fileName.toLowerCase().contains("backup")) {
                        System.out.println("⚠️ Archivo sensible encontrado: " + fileName);
                        sensitiveFiles.add(fileName);
                    }
                }
            }

            ftpClient.logout();
            ftpClient.disconnect();
            System.out.println("✅ Análisis FTP completado.");

        } catch (IOException e) {
            System.err.println("❌ Error al conectar al servidor FTP: " + e.getMessage());
        }

        return sensitiveFiles;
    }
}
