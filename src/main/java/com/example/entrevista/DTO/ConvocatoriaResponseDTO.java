package com.example.entrevista.DTO;

import com.example.entrevista.model.Category;
import com.example.entrevista.model.ExperienceLevel;
import com.example.entrevista.model.WorkMode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvocatoriaResponseDTO {
    
    private Long id;
    private String jobTitle;
    private Category category;
    private String jobDescription;
    private String technicalRequirements;
    private ExperienceLevel experienceLevel;
    private WorkMode workMode;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String benefitsPerks;
    private LocalDate publicationDate;
    private LocalDate closingDate;
    private boolean activo;
    private int dificultad;
    private Long empresaId;
    private String empresaNombre; // Para mostrar el nombre de la empresa
    
    // Computed fields
    private String formattedSalaryRange;
    private boolean isActive;
    private long daysUntilClosing;
    
    // Status information
    private String status; // "ACTIVE", "CLOSING_SOON", "CLOSED", "DRAFT"
    
    public void computeFields() {
        // Format salary range
        if (salaryMin != null && salaryMax != null) {
            this.formattedSalaryRange = String.format("%s %,.2f - %,.2f", 
                salaryCurrency != null ? salaryCurrency : "USD", 
                salaryMin, salaryMax);
        } else if (salaryMin != null) {
            this.formattedSalaryRange = String.format("%s %,.2f+", 
                salaryCurrency != null ? salaryCurrency : "USD", 
                salaryMin);
        } else {
            this.formattedSalaryRange = "Salary not specified";
        }
        
        // Check if active
        this.isActive = activo && closingDate != null && closingDate.isAfter(LocalDate.now());
        
        // Calculate days until closing
        if (closingDate != null) {
            this.daysUntilClosing = LocalDate.now().until(closingDate).getDays();
        }
        
        // Determine status
        if (!activo) {
            this.status = "DRAFT";
        } else if (closingDate != null && closingDate.isBefore(LocalDate.now())) {
            this.status = "CLOSED";
        } else if (daysUntilClosing <= 7) {
            this.status = "CLOSING_SOON";
        } else {
            this.status = "ACTIVE";
        }
    }
}
