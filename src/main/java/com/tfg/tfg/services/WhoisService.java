package com.tfg.tfg.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Service
public class WhoisService {

    public String lookup(String domain) {
        StringBuilder result = new StringBuilder();
        String whoisServer = "whois.verisign-grs.com"; // para .com y .net

        try (Socket socket = new Socket(whoisServer, 43);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(domain);

            String line;
            while ((line = in.readLine()) != null) {
                result.append(line).append("\n");
            }

        } catch (Exception e) {
            return "Error al consultar WHOIS: " + e.getMessage();
        }

        return result.toString();
    }
}
