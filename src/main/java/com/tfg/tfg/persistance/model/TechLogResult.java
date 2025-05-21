package com.tfg.tfg.persistance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tech_log_result")
public class TechLogResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String technology;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_log_id")
    private TechLog techLog;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }
    public TechLog getTechLog() { return techLog; }
    public void setTechLog(TechLog techLog) { this.techLog = techLog; }
}
