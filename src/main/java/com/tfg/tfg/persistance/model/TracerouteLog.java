package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "traceroute_log")
public class TracerouteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String target;

    @Column(columnDefinition = "TEXT")
    private String result;

    private String toolUsed;

    @Column(columnDefinition = "timestamp with time zone")
    private Instant timestamp;

    private String userAgent;

    private Boolean isBot;

    @Column(columnDefinition = "TEXT")
    private String location;

    private String ipAddress;

    private String internalIpAddress;

    private String action;

    @OneToMany(mappedBy = "tracerouteLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TracerouteLogResult> results;

    // Getters y setters
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

    public Boolean getIsBot() { return isBot; }
    public void setIsBot(Boolean isBot) { this.isBot = isBot; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getInternalIpAddress() { return internalIpAddress; }
    public void setInternalIpAddress(String internalIpAddress) { this.internalIpAddress = internalIpAddress; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public List<TracerouteLogResult> getResults() { return results; }
    public void setResults(List<TracerouteLogResult> results) { this.results = results; }
}
