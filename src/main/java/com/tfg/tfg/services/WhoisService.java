package com.tfg.tfg.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Service  // Indica que esta clase es un servicio de Spring y puede ser inyectada como dependencia
public class WhoisService {

    // Método que realiza una consulta WHOIS para un dominio dado
    public String lookup(String domain) {
        StringBuilder result = new StringBuilder();  // Acumulador para la respuesta WHOIS
        String whoisServer = "whois.verisign-grs.com";  // Servidor WHOIS para dominios .com y .net

        // Abrir un socket al puerto 43 del servidor WHOIS
        try (
            Socket socket = new Socket(whoisServer, 43);  // Conectar al servidor WHOIS en el puerto 43
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);  // Para enviar datos al servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))  // Para leer la respuesta
        ) {

            out.println(domain);  // Enviar el dominio a consultar al servidor WHOIS

            String line;
            // Leer la respuesta línea por línea y añadirla al resultado
            while ((line = in.readLine()) != null) {
                result.append(line).append("\n");
            }

        } catch (Exception e) {
            // En caso de error, devolver mensaje amigable con la excepción
            return "Error al consultar WHOIS: " + e.getMessage();
        }

        // Devolver el resultado completo como string
        return result.toString();
    }
}
