package com.example.entrevista.DTO;

import com.example.entrevista.model.Category;
import com.example.entrevista.model.ExperienceLevel;
import com.example.entrevista.model.WorkMode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvocatoriaCreateDTO {
    @NotBlank(message = "El título del trabajo es obligatorio")
    private String jobTitle;

    @NotNull(message = "La categoría es obligatoria")
    @Min(value = 1, message = "La categoría debe ser un número entre 1-8")
    @Max(value = 8, message = "La categoría debe ser un número entre 1-8")
    private Integer categoryNumber; // Cambio: ahora recibe número

    @NotBlank(message = "La descripción del trabajo es obligatoria")
    private String jobDescription;

    private String technicalRequirements;

    @NotNull(message = "El nivel de experiencia es obligatorio")
    @Min(value = 1, message = "El nivel de experiencia debe ser un número entre 1-4")
    @Max(value = 4, message = "El nivel de experiencia debe ser un número entre 1-4")
    private Integer experienceLevelNumber; // Cambio: ahora recibe número

    @NotNull(message = "La modalidad de trabajo es obligatoria")
    @Min(value = 1, message = "La modalidad de trabajo debe ser un número entre 1-3")
    @Max(value = 3, message = "La modalidad de trabajo debe ser un número entre 1-3")
    private Integer workModeNumber; // Cambio: ahora recibe número

    private String location;

    @DecimalMin(value = "0.0", message = "El salario mínimo no puede ser negativo")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "El salario máximo no puede ser negativo")
    private BigDecimal salaryMax;

    private String salaryCurrency = "USD";

    private String benefitsPerks;

    @NotNull(message = "La fecha de publicación es obligatoria")
    private LocalDate publicationDate;

    @NotNull(message = "La fecha de cierre es obligatoria")
    private LocalDate closingDate;

    @Min(value = 1, message = "La dificultad debe estar entre 1 y 10")
    @Max(value = 10, message = "La dificultad debe estar entre 1 y 10")
    private int dificultad = 1;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    // Métodos de conversión
    public Category getCategory() {
        return categoryNumber != null ? Category.fromValue(categoryNumber) : null;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevelNumber != null ? ExperienceLevel.fromValue(experienceLevelNumber) : null;
    }

    public WorkMode getWorkMode() {
        return workModeNumber != null ? WorkMode.fromValue(workModeNumber) : null;
    }

    // Custom validation methods
    public boolean isSalaryRangeValid() {
        if (salaryMin != null && salaryMax != null) {
            return salaryMax.compareTo(salaryMin) >= 0;
        }
        return true;
    }

    public boolean isDateRangeValid() {
        if (publicationDate != null && closingDate != null) {
            return closingDate.isAfter(publicationDate);
        }
        return true;
    }

    public boolean isValid() {
        return jobTitle != null && !jobTitle.trim().isEmpty() &&
               categoryNumber != null &&
               jobDescription != null && !jobDescription.trim().isEmpty() &&
               experienceLevelNumber != null &&
               workModeNumber != null &&
               publicationDate != null &&
               closingDate != null &&
               empresaId != null &&
               isSalaryRangeValid() &&
               isDateRangeValid();
    }
}
