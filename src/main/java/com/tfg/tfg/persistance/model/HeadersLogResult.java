package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "headers_log_result")
public class HeadersLogResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String header;

    @Column(columnDefinition = "TEXT")  // para asegurar texto largo
    private String value;

    @ManyToOne
    @JoinColumn(name = "headers_log_id")
    private HeadersLog headersLog;

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public HeadersLog getHeadersLog() { return headersLog; }
    public void setHeadersLog(HeadersLog headersLog) { this.headersLog = headersLog; }
}
