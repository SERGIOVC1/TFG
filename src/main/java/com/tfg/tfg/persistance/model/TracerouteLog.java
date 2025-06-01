package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "traceroute_log")
public class TracerouteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String target;

    @Lob
    private String result;

    @Column(name = "tool_used")
    private String toolUsed;

    private Instant timestamp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "is_bot")
    private boolean isBot;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "internal_ip_address")
    private String internalIpAddress;

    private String location;

    private String action;

    private String details;

    @Column(name = "user_id")
    private String userId;

    // Getters y setters
    // ... (añade getters y setters para userId)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getToolUsed() { return toolUsed; }
    public void setToolUsed(String toolUsed) { this.toolUsed = toolUsed; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public boolean isBot() { return isBot; }
    public void setIsBot(boolean isBot) { this.isBot = isBot; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getInternalIpAddress() { return internalIpAddress; }
    public void setInternalIpAddress(String internalIpAddress) { this.internalIpAddress = internalIpAddress; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
