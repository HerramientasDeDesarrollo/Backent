package com.example.entrevista.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Convocatoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Job Title
    @Column(name = "job_title", nullable = false, length = 255)
    private String jobTitle;

    // Category
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    // Job Description
    @Lob
    @Column(name = "job_description", nullable = false)
    private String jobDescription;

    // Technical Requirements
    @Lob
    @Column(name = "technical_requirements")
    private String technicalRequirements;

    // Experience Level
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel;

    // Work Mode
    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false)
    private WorkMode workMode;

    // Location
    @Column(name = "location", length = 255)
    private String location;

    // Salary Range
    @Column(name = "salary_min", precision = 10, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 10, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency; // USD, EUR, MXN, etc.

    // Benefits & Perks
    @Lob
    @Column(name = "benefits_perks")
    private String benefitsPerks;

    // Publication Date and Closing Date
    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;

    // Status and metadata
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "dificultad", nullable = false)
    private int dificultad = 1; // 1-10 scale

    // Relationship with Company
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // Utility methods
    public String getFormattedSalaryRange() {
        if (salaryMin != null && salaryMax != null) {
            return String.format("%s %,.2f - %,.2f", 
                salaryCurrency != null ? salaryCurrency : "USD", 
                salaryMin, salaryMax);
        } else if (salaryMin != null) {
            return String.format("%s %,.2f+", 
                salaryCurrency != null ? salaryCurrency : "USD", 
                salaryMin);
        }
        return "Salary not specified";
    }

    public boolean isActive() {
        return activo && closingDate.isAfter(LocalDate.now());
    }

    public long getDaysUntilClosing() {
        return LocalDate.now().until(closingDate).getDays();
    }
}
