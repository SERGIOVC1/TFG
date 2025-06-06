// Servicio encargado de escanear directorios web usando la herramienta Gobuster

package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.WebDirectoryLog;
import com.tfg.tfg.persistance.repository.WebDirectoryLogRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service  // Anotación de Spring para indicar que esta clase es un servicio
public class WebDirectoryScannerService {

    private final WebDirectoryLogRepository logRepository;

    // Inyección del repositorio a través del constructor
    public WebDirectoryScannerService(WebDirectoryLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    // Método que realiza el escaneo de directorios y envía resultados en tiempo real al cliente usando SSE
    public SseEmitter scanDirectories(String target, String userId) {
        SseEmitter emitter = new SseEmitter();  // Permite enviar eventos al cliente de forma asíncrona

        // Ejecutamos el escaneo en un hilo separado para no bloquear el hilo principal
        new Thread(() -> {
            try {
                // Cargar el ejecutable de gobuster y la lista de palabras desde los recursos del proyecto
                File gobusterFile = new ClassPathResource("bin/gobuster.exe").getFile();
                File wordlistFile = new ClassPathResource("wordlists/custom-wordlist-5000.txt").getFile();

                // Construir el comando a ejecutar (scan tipo dir con extensiones .php y .html, 2 hilos)
                String command = "\"" + gobusterFile.getAbsolutePath() + "\" dir --url " + target +
                        " --wordlist \"" + wordlistFile.getAbsolutePath() + "\" -x php,html -t 2";

                // Crear y lanzar el proceso (en Windows)
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
                pb.redirectErrorStream(true);  // Redirige errores al flujo estándar
                Process process = pb.start();  // Inicia el proceso

                // Preparar para leer la salida del proceso
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                // Patrón para eliminar secuencias ANSI (formato de consola)
                Pattern ansiPattern = Pattern.compile("\u001B\\[[;\\d]*m|\u001B\\[2K");

                // Patrón para detectar resultados válidos del escaneo (ruta + código de estado HTTP)
                Pattern resultPattern = Pattern.compile("^(\\/\\S+)\\s+\\(Status:\\s*(\\d{3})\\).*");

                // Obtener información de IPs y localización
                String publicIp = getPublicIp();
                String internalIp = InetAddress.getLocalHost().getHostAddress();
                String location = getLocation();

                String line;
                // Leer la salida línea por línea
                while ((line = reader.readLine()) != null) {
                    line = ansiPattern.matcher(line).replaceAll("");  // Limpiar línea de caracteres ANSI
                    emitter.send(line);  // Enviar la línea al cliente vía SSE

                    // Comprobar si la línea coincide con un resultado válido
                    Matcher matcher = resultPattern.matcher(line);
                    if (matcher.find()) {
                        int status = Integer.parseInt(matcher.group(2));
                        // Solo guardar si el código de estado es interesante
                        if (status == 200 || status == 301 || status == 302 || status == 303) {
                            WebDirectoryLog log = new WebDirectoryLog();
                            log.setUserId(userId);  // Usuario que ejecutó el escaneo
                            log.setAction("Directory Scan");  // Acción registrada
                            log.setDetails(target);  // URL objetivo del escaneo
                            log.setIpAddress(publicIp);  // IP pública del servidor
                            log.setInternalIpAddress(internalIp);  // IP local del servidor
                            log.setResult(line.trim());  // Resultado completo de esa línea
                            log.setToolUsed("gobuster");  // Herramienta utilizada
                            log.setTimestamp(System.currentTimeMillis());  // Tiempo actual
                            log.setUserAgent(System.getProperty("http.agent"));  // Agente HTTP (puede ser null)
                            log.setIsBot(false);  // Indica que no es un bot
                            log.setLocation(location);  // Localización estimada

                            logRepository.save(log);  // Guardar el resultado en la base de datos
                        }
                    }
                }

                process.waitFor();  // Esperar a que el proceso termine
                emitter.complete();  // Finalizar la conexión SSE
            } catch (Exception e) {
                emitter.completeWithError(e);  // Informar al cliente en caso de error
            }
        }).start();

        return emitter;  // Devolver el emisor para que el controlador pueda enviarlo al cliente
    }

    // Método auxiliar para obtener la IP pública
    private String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    // Método auxiliar para obtener ubicación aproximada mediante IP
    private String getLocation() {
        try {
            URL url = new URL("https://ipapi.co/json/");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }

                String jsonStr = json.toString();

                // Extraer ciudad y país del JSON manualmente (no se usa parser)
                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*") ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*") ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";
                String location = (city + ", " + country).trim();

                return location.isBlank() ? "Desconocida" : location;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
