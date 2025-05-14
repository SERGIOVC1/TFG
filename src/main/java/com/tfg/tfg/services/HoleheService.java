package com.tfg.tfg.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class HoleheService {

    public String runHolehe(String email) {
        StringBuilder output = new StringBuilder();

        try {
            // ✅ Obtener la ruta del wrapper desde resources/bin/
            File batFile = new ClassPathResource("bin/holehe-wrapper.bat").getFile();
            String batPath = batFile.getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", batPath, email);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("⚠️ Error al ejecutar holehe. Código de salida: ").append(exitCode);
            }

        } catch (Exception e) {
            output.append("❌ Error al ejecutar holehe: ").append(e.getMessage());
        }

        return output.toString();
    }
}
