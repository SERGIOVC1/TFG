package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tech_log")
public class TechLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private String result;
    private String toolUsed;
    private Long timestamp;
    private String userAgent;
    private Boolean isBot;
    private String location;
    private String action;
    private String details;
    private String ipAddress;
    private String internalIpAddress;

    @OneToMany(mappedBy = "techLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TechLogResult> results;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getToolUsed() { return toolUsed; }
    public void setToolUsed(String toolUsed) { this.toolUsed = toolUsed; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Boolean getIsBot() { return isBot; }
    public void setIsBot(Boolean isBot) { this.isBot = isBot; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getInternalIpAddress() { return internalIpAddress; }
    public void setInternalIpAddress(String internalIpAddress) { this.internalIpAddress = internalIpAddress; }
    public List<TechLogResult> getResults() { return results; }
    public void setResults(List<TechLogResult> results) { this.results = results; }
}
