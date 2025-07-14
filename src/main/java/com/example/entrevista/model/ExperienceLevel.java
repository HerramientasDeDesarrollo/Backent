package com.example.entrevista.model;

public enum ExperienceLevel {
    ENTRY_LEVEL(1, "Entry Level (0-2 years)"),
    MID_LEVEL(2, "Mid Level (3-5 years)"),
    SENIOR_LEVEL(3, "Senior Level (6-8 years)"),
    LEAD_LEVEL(4, "Lead Level (9+ years)");
    
    private final int value;
    private final String displayName;
    
    ExperienceLevel(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    // Método para obtener enum por valor numérico
    public static ExperienceLevel fromValue(int value) {
        for (ExperienceLevel level : ExperienceLevel.values()) {
            if (level.getValue() == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid ExperienceLevel value: " + value);
    }
    
    // Método para obtener rango de años
    public String getYearsRange() {
        switch (this) {
            case ENTRY_LEVEL: return "0-2 years";
            case MID_LEVEL: return "3-5 years";
            case SENIOR_LEVEL: return "6-8 years";
            case LEAD_LEVEL: return "9+ years";
            default: return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
