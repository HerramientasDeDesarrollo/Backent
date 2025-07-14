package com.example.entrevista.model;

public enum WorkMode {
    REMOTE(1, "Remote"),
    ON_SITE(2, "On-site"), 
    HYBRID(3, "Hybrid");
    
    private final int value;
    private final String displayName;
    
    WorkMode(int value, String displayName) {
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
    public static WorkMode fromValue(int value) {
        for (WorkMode mode : WorkMode.values()) {
            if (mode.getValue() == value) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid WorkMode value: " + value);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
