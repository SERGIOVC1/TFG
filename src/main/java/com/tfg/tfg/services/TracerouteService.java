package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.TracerouteLog;
import com.tfg.tfg.persistance.model.TracerouteLogResult;
import com.tfg.tfg.persistance.repository.TracerouteLogRepository;
import com.tfg.tfg.persistance.repository.TracerouteLogResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service  // Marca esta clase como un servicio de Spring para su inyección automática
public class TracerouteService {

    @Autowired  // Inyección del repositorio para guardar logs de traceroute
    private TracerouteLogRepository tracerouteLogRepository;

    @Autowired  // Inyección del repositorio para guardar resultados individuales del traceroute
    private TracerouteLogResultRepository tracerouteLogResultRepository;

    // Método para obtener la IP pública del servidor actual desde un servicio externo
    private String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");  // Servicio que devuelve la IP pública
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return in.readLine();  // Leer y devolver la respuesta
            }
        } catch (Exception e) {
            return "Desconocida";  // En caso de error, devolver valor por defecto
        }
    }

    // Método para obtener la localización geográfica aproximada a partir de la IP pública
    private String getLocation() {
        try {
            URL url = new URL("https://ipapi.co/json/");  // Servicio de geolocalización IP
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);  // Leer todo el JSON de la respuesta
                }
                String jsonStr = json.toString();

                // Extraer la ciudad usando una expresión regular
                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";

                // Extraer el país usando una expresión regular
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";

                // Construir la localización y devolverla
                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";  // En caso de error, devolver valor por defecto
        }
    }

    // Método principal que simula la ejecución de un traceroute, guarda el log y los resultados
    public List<String> executeTraceroute(String target, String userId) {
        List<String> tracerouteOutput = new ArrayList<>();

        // Simulación de salida del comando traceroute (debería reemplazarse por ejecución real)
        tracerouteOutput.add("142.250.184.174 Madrid, Spain Google");
        tracerouteOutput.add("212.166.147.222 Madrid, Spain Desconocido");
        tracerouteOutput.add("108.170.252.253 Montreal, Canada Google");

        try {
            // Crear objeto log para almacenar los datos principales del traceroute
            TracerouteLog log = new TracerouteLog();
            log.setTarget(target);  // IP o dominio destino
            log.setResult(String.join("\n", tracerouteOutput));  // Salida completa como string
            log.setToolUsed("traceroute");  // Herramienta utilizada
            log.setTimestamp(Instant.now());  // Fecha y hora actual
            log.setUserAgent(System.getProperty("http.agent"));  // Información del agente HTTP (puede ser null)
            log.setIsBot(false);  // Indica que no es un bot
            log.setIpAddress(getPublicIp());  // Obtener IP pública
            log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());  // Obtener IP local del servidor
            log.setLocation(getLocation());  // Obtener localización basada en IP
            log.setAction("Traceroute ejecutado");  // Descripción de la acción

            // Si se proporciona userId, lo guarda en el log
            if (userId != null && !userId.isEmpty()) {
                log.setUserId(userId);
            }

            // Guardar el log principal en la base de datos
            TracerouteLog savedLog = tracerouteLogRepository.save(log);

            // Procesar la salida simulada para crear resultados individuales
            List<TracerouteLogResult> results = new ArrayList<>();
            for (String line : tracerouteOutput) {
                String[] parts = line.split(" ", 4);  // Separar IP, ciudad, país y proveedor
                if (parts.length == 4) {
                    TracerouteLogResult res = new TracerouteLogResult();
                    res.setIp(parts[0]);
                    res.setCity(parts[1].replace(",", ""));  // Quitar coma de la ciudad
                    res.setCountry(parts[2]);
                    res.setProvider(parts[3]);
                    res.setTracerouteLog(savedLog);  // Enlazar con el log principal
                    results.add(res);
                }
            }

            // Guardar todos los resultados individuales en la base de datos
            tracerouteLogResultRepository.saveAll(results);

        } catch (Exception e) {
            e.printStackTrace();  // Imprimir errores en consola (debería usar logger en producción)
        }

        return tracerouteOutput;  // Devolver la salida del traceroute (simulada)
    }

    // Método para obtener todos los logs o solo los de un usuario específico
    public List<TracerouteLog> getLogsByUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return tracerouteLogRepository.findAll();  // Si no se indica userId, devolver todos
        } else {
            return tracerouteLogRepository.findByUserId(userId);  // Si se indica, devolver solo los suyos
        }
    }
}
