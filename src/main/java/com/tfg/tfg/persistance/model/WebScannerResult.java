package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "web_scanner_result")
public class WebScannerResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "log_id", nullable = false)
    private WebScannerLog log;

    private String port;
    private String state;
    private String service;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WebScannerLog getLog() {
        return log;
    }

    public void setLog(WebScannerLog log) {
        this.log = log;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
