package com.tfg.tfg.dto;

public class ScanRequest {
    private String target;
    private String tool;

    // Constructor
    public ScanRequest(String target, String tool) {
        this.target = target;
        this.tool = tool;
    }

    // Getters y Setters
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }
}
