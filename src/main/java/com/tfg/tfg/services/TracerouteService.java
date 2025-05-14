package com.tfg.tfg.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class TracerouteService {

    public List<String> executeTraceroute(String target) {
        List<String> output = new ArrayList<>();

        try {
            // Detectar sistema operativo
            String os = System.getProperty("os.name").toLowerCase();
            String command = os.contains("win") ? "tracert" : "traceroute";

            // Normalizar dominio (quitar protocolo si existe)
            target = normalizeTarget(target);

            // Ejecutar comando
            ProcessBuilder builder = new ProcessBuilder(command, target);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Leer salida
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }

            process.waitFor();
        } catch (Exception e) {
            output.add("❌ Error al ejecutar traceroute: " + e.getMessage());
        }

        return output;
    }

    private String normalizeTarget(String input) {
        return input.replaceFirst("^(https?://)", "").split("/")[0].trim();
    }
}
