package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.HoleheLog;
import com.tfg.tfg.persistance.repository.HoleheLogRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.time.Instant;

@Service  // Marca la clase como servicio gestionado por Spring
public class HoleheService {

    private final HoleheLogRepository holeheLogRepository;  // Repositorio para guardar logs en base de datos

    // Constructor para inyección del repositorio
    public HoleheService(HoleheLogRepository holeheLogRepository) {
        this.holeheLogRepository = holeheLogRepository;
    }

    /**
     * Ejecuta la herramienta externa 'holehe' para escanear un email.
     * @param email email a escanear
     * @return salida completa de la ejecución como String
     */
    public String runHolehe(String email) {
        StringBuilder output = new StringBuilder();

        try {
            // Obtiene el archivo .bat desde recursos para ejecutarlo
            File batFile = new ClassPathResource("bin/holehe-wrapper.bat").getFile();
            String batPath = batFile.getAbsolutePath();

            // Crea un proceso para ejecutar el comando bat con el email como argumento
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", batPath, email);
            pb.redirectErrorStream(true); // Redirige errores a la salida estándar

            Process process = pb.start();  // Inicia el proceso

            StringBuilder resultData = new StringBuilder();

            // Lee línea a línea la salida del proceso
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");  // Guarda toda la salida

                    // Filtra líneas relevantes que empiezan por [+], ignorando ciertos textos
                    if (line.trim().startsWith("[+]") &&
                        !line.contains("Email used") &&
                        !line.contains("Email not used") &&
                        !line.contains("Rate limit")) {
                        resultData.append(line.trim()).append(", ");
                    }
                }
            }

            // Espera a que el proceso termine y revisa código de salida
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("⚠️ Error al ejecutar holehe. Código de salida: ").append(exitCode);
            }

            // Quita la última coma y espacio de resultData
            String result = resultData.toString().replaceAll(",\\s*$", "");

            // Si hay resultado relevante, crea y guarda el log en la base de datos
            if (!result.isEmpty()) {
                HoleheLog log = new HoleheLog();
                log.setAction("Email Scan");
                log.setDetails(email);
                log.setIpAddress(getPublicIp());  // Obtiene IP pública actual
                log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());  // IP interna local
                log.setResult(result);
                log.setToolUsed("holehe");
                log.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));  // Timestamp actual
                log.setUserAgent(System.getProperty("http.agent"));  // Agente HTTP (browser info)
                log.setIsBot(false);
                log.setLocation(getLocation());  // Ubicación aproximada por IP

                holeheLogRepository.save(log);  // Guarda en BD
            }

        } catch (Exception e) {
            e.printStackTrace(); // Imprime error en consola si falla ejecución
            output.append("❌ Error al ejecutar holehe: ").append(e.getMessage());
        }

        return output.toString();  // Devuelve toda la salida del comando
    }

    /**
     * Método para obtener la IP pública consultando un servicio externo
     * @return IP pública como String o "Desconocida" si falla
     */
    private String getPublicIp() {
        try {
            var url = new java.net.URL("https://api.ipify.org");
            try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Desconocida";
        }
    }

    /**
     * Método para obtener la ubicación aproximada consultando servicio externo
     * @return ubicación (ciudad, país) o "Desconocida" si falla
     */
    private String getLocation() {
        try {
            var url = new java.net.URL("https://ipapi.co/json/");
            try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }

                String jsonStr = json.toString();

                // Extrae ciudad y país usando expresiones regulares simples
                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*") ?
                        jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*") ?
                        jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";

                String location = (city + ", " + country).trim();
                return location.isBlank() ? "Desconocida" : location;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log de error para debugging
            return "Desconocida";
        }
    }
}
