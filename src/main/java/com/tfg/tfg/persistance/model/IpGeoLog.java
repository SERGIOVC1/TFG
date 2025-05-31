package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "geoip_log")
public class IpGeoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")    // Añadido userId
    private String userId;

    @Column(name = "ip_address")
    private String ipAddress;

    private String result;

    @Column(name = "tool_used")
    private String toolUsed;

    private Long timestamp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "is_bot")
    private boolean isBot;

    private String location;

    @Column(name = "internal_ip_address")
    private String internalIpAddress;

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getToolUsed() { return toolUsed; }
    public void setToolUsed(String toolUsed) { this.toolUsed = toolUsed; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public boolean isBot() { return isBot; }
    public void setBot(boolean bot) { isBot = bot; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getInternalIpAddress() { return internalIpAddress; }
    public void setInternalIpAddress(String internalIpAddress) { this.internalIpAddress = internalIpAddress; }
}
