package com.tfg.tfg.dto;

public class IpGeoLogRequest {
    private String ipAddress;
    private String result;
    private String toolUsed;
    private Long timestamp;
    private String userAgent;
    private boolean isBot;
    private String location;
    private String internalIpAddress;

    // Getters y Setters
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
