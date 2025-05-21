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
import java.util.ArrayList;
import java.util.List;

@Service
public class TracerouteService {

    @Autowired
    private TracerouteLogRepository tracerouteLogRepository;

    @Autowired
    private TracerouteLogResultRepository tracerouteLogResultRepository;

    // Método para obtener IP pública
    private String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return in.readLine();
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    // Método para obtener ubicación del cliente
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

                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";

                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    public List<String> executeTraceroute(String target) {
        List<String> tracerouteOutput = new ArrayList<>();

        // Aquí agregarías el código real para ejecutar traceroute y obtener el output (simulación)
        // Por ejemplo, llamando a un proceso externo o usando librerías Java que hagan traceroute.
        // Para el ejemplo, añadimos salida simulada:
        tracerouteOutput.add("142.250.184.174 Madrid, Spain Google");
        tracerouteOutput.add("212.166.147.222 Madrid, Spain Desconocido");
        tracerouteOutput.add("108.170.252.253 Montreal, Canada Google");

        // Guardar log y resultados en base de datos
        try {
            TracerouteLog log = new TracerouteLog();
            log.setTarget(target);
            log.setResult(String.join("\n", tracerouteOutput));
            log.setToolUsed("traceroute");
            log.setTimestamp(System.currentTimeMillis());
            log.setUserAgent(System.getProperty("http.agent"));
            log.setIsBot(false);
            log.setIpAddress(getPublicIp());
            log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
            log.setLocation(getLocation());
            log.setAction("Traceroute ejecutado");

            TracerouteLog savedLog = tracerouteLogRepository.save(log);

            // Parsear y guardar cada salto
            List<TracerouteLogResult> results = new ArrayList<>();
            for (String line : tracerouteOutput) {
                // Suponemos formato: "IP Ciudad País Proveedor"
                String[] parts = line.split(" ", 4);
                if (parts.length == 4) {
                    TracerouteLogResult res = new TracerouteLogResult();
                    res.setIp(parts[0]);
                    res.setCity(parts[1].replace(",", ""));
                    res.setCountry(parts[2]);
                    res.setProvider(parts[3]);
                    res.setTracerouteLog(savedLog);
                    results.add(res);
                }
            }
            tracerouteLogResultRepository.saveAll(results);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tracerouteOutput;
    }
}
