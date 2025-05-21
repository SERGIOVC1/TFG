package com.tfg.tfg.dto;

import java.time.Instant;

public class AuditLogDto {

    private String action;
    private String tableName;
    private Instant timestamp;
    private String details;

    // Constructor
    public AuditLogDto(String action, String tableName, Instant timestamp, String details) {
        this.action = action;
        this.tableName = tableName;
        this.timestamp = timestamp;
        this.details = details;
    }

    // Getters y setters

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
