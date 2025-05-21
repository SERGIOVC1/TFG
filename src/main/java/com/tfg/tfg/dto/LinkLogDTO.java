package com.tfg.tfg.dto;

import java.time.Instant;

public class LinkLogDTO {

    private String ip;
    private String userAgent;
    private Instant timestamp;

    public LinkLogDTO() {}

    public LinkLogDTO(String ip, String userAgent, Instant timestamp) {
        this.ip = ip;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
    }

    // Getters y setters
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
