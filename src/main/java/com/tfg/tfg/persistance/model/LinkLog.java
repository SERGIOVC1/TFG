package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "link_log")
public class LinkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_ip", nullable = false)
    private String clientIp;

    @Column(name = "server_public_ip", nullable = false)
    private String serverPublicIp;

    @Column(name = "server_local_ip", nullable = false)
    private String serverLocalIp;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    public LinkLog() {}

    // Constructor para conveniencia (opcional)
    public LinkLog(String clientIp, String userAgent, String code) {
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.code = code;
        this.timestamp = Instant.now().toEpochMilli();
        this.serverPublicIp = "Desconocida";
        this.serverLocalIp = "Desconocida";
    }

    public Long getId() {
        return id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getServerPublicIp() {
        return serverPublicIp;
    }

    public void setServerPublicIp(String serverPublicIp) {
        this.serverPublicIp = serverPublicIp;
    }

    public String getServerLocalIp() {
        return serverLocalIp;
    }

    public void setServerLocalIp(String serverLocalIp) {
        this.serverLocalIp = serverLocalIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
