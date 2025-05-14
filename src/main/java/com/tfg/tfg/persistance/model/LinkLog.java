// src/main/java/com/tfg/tfg/persistance/model/LinkLog.java
package com.tfg.tfg.persistance.model;

import java.time.LocalDateTime;

public class LinkLog {
    private String ip;
    private String userAgent;
    private LocalDateTime timestamp;
    private String code;

    public LinkLog(String ip, String userAgent, String code) {
        this.ip = ip;
        this.userAgent = userAgent;
        this.timestamp = LocalDateTime.now();
        this.code = code;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }
}
