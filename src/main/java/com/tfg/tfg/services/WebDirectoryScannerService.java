package com.tfg.tfg.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.util.regex.Pattern;

@Service
public class WebDirectoryScannerService {

    public SseEmitter scanDirectories(String target) {
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                // ✅ Obtener la ruta del ejecutable y wordlist dentro del proyecto
                File gobusterFile = new ClassPathResource("bin/gobuster.exe").getFile();
                File wordlistFile = new ClassPathResource("wordlists/custom-wordlist-5000.txt").getFile();

                // ✅ Construcción del comando para Windows
                String command = "\"" + gobusterFile.getAbsolutePath() + "\" dir --url " + target +
                                 " --wordlist \"" + wordlistFile.getAbsolutePath() + "\" -x php,html -t 10";

                // ✅ Ejecutar el comando en Windows
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                // 🛑 Expresión regular para eliminar secuencias ANSI innecesarias
                Pattern ansiPattern = Pattern.compile("\u001B\\[[;\\d]*m|\u001B\\[2K");

                while ((line = reader.readLine()) != null) {
                    line = ansiPattern.matcher(line).replaceAll("");

                    if (!line.contains("Progress:")) {
                        emitter.send(line);
                    }
                }

                process.waitFor();
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
