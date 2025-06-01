// WebDirectoryLog.java
package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gobuster_log")
public class WebDirectoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String ipAddress;
    private String internalIpAddress;
    private String action;
    private String details;
    private String result;
    private String toolUsed;
    private Long timestamp;
    private String userAgent;
    private boolean isBot;
    private String location;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getInternalIpAddress() { return internalIpAddress; }
    public void setInternalIpAddress(String internalIpAddress) { this.internalIpAddress = internalIpAddress; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getToolUsed() { return toolUsed; }
    public void setToolUsed(String toolUsed) { this.toolUsed = toolUsed; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public boolean isBot() { return isBot; }
    public void setIsBot(boolean isBot) { this.isBot = isBot; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
