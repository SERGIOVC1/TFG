package com.tfg.tfg.dto;

public class WhoisLogRequest {

    private String domain;
    private String whoisResult;
    private String toolUsed;
    private Long timestamp;
    private String userAgent;
    private boolean isBot;
    private String location;
    private String action;
    private String details;
    private String internalIpAddress;
    private String ipAddress;

    // Getters y Setters

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getWhoisResult() {
        return whoisResult;
    }

    public void setWhoisResult(String whoisResult) {
        this.whoisResult = whoisResult;
    }

    public String getToolUsed() {
        return toolUsed;
    }

    public void setToolUsed(String toolUsed) {
        this.toolUsed = toolUsed;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getInternalIpAddress() {
        return internalIpAddress;
    }

    public void setInternalIpAddress(String internalIpAddress) {
        this.internalIpAddress = internalIpAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
