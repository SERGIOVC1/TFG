package com.tfg.tfg.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import org.springframework.stereotype.Service;

@Service
public class NetworkScannerService {

    // Método para resolver la URL a IP
    public String resolveUrlToIp(String url) throws Exception {
        try {
            InetAddress inetAddress = InetAddress.getByName(url);  // Resuelve la URL
            return inetAddress.getHostAddress();  // Devuelve la dirección IP correspondiente
        } catch (Exception e) {
            throw new Exception("Error al resolver la URL: " + e.getMessage());
        }
    }

    // Método para realizar el escaneo de red
    public String scanNetwork(String target, String scanType) throws Exception {
        try {
            String ip = target;
            
            // Si el target es una URL, resolvemos a IP
            if (isValidUrl(target)) {
                ip = resolveUrlToIp(target);  // Resolver la URL a IP
            }

            // Comando a ejecutar dependiendo del tipo de mapeo
            String command = buildCommand(scanType, ip);
            Process process = Runtime.getRuntime().exec(command);

            // Leer la salida del proceso
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Esperar a que termine el escaneo
            process.waitFor();
            return output.toString();  // Retorna los resultados del escaneo
        } catch (IOException | InterruptedException e) {
            return "Error al ejecutar Nmap: " + e.getMessage();
        }
    }

    // Función para verificar si una cadena es una URL válida
    private boolean isValidUrl(String url) {
        String regex = "^(https?://)?([a-z0-9-]+\\.)+[a-z0-9]{2,4}(:[0-9]{1,5})?(\\/.*)?$";
        return url.matches(regex);  // Comprueba si es una URL válida
    }

    // Método para construir el comando de nmap basado en el tipo de escaneo
    private String buildCommand(String scanType, String target) {
        switch (scanType) {
            case "basic":
                return "nmap  -sV -sC " + target;
            case "intermediate":
                return "nmap -sV -sC -sS " + target;
            case "deep":
                return "nmap -sV -sC -T4 -A " + target;
            default:
                return "nmap -sV -sC " + target;  // Escaneo básico por defecto
        }
    }
}
