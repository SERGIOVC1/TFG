package com.tfg.tfg.dto;

public class WebDirectoryDTO {

    private String result;
    private String details;
    private Long timestamp;

    public WebDirectoryDTO() {}

    public WebDirectoryDTO(String result, String details, Long timestamp) {
        this.result = result;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
