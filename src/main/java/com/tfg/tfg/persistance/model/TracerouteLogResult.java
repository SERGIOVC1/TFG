package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "traceroute_log_result")
public class TracerouteLogResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ip;

    private String city;

    private String country;

    private String provider;

    @ManyToOne
    @JoinColumn(name = "traceroute_log_id")
    private TracerouteLog tracerouteLog;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public TracerouteLog getTracerouteLog() { return tracerouteLog; }
    public void setTracerouteLog(TracerouteLog tracerouteLog) { this.tracerouteLog = tracerouteLog; }
}
