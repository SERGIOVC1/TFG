package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Scan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String target; // Dirección objetivo del escaneo
    private String toolUsed; // Herramienta usada (Nmap, OWASP ZAP, etc.)
    private String result; // Resultado del escaneo
    private LocalDateTime timestamp; // Fecha y hora del escaneo

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getToolUsed() {
        return toolUsed;
    }

    public void setToolUsed(String toolUsed) {
        this.toolUsed = toolUsed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
