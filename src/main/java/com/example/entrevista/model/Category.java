package com.example.entrevista.model;

public enum Category {
    TECHNOLOGY(1, "Technology"),
    DESIGN(2, "Design"),
    MARKETING(3, "Marketing"),
    SALES(4, "Sales"),
    FINANCE(5, "Finance"),
    OPERATIONS(6, "Operations"),
    HUMAN_RESOURCES(7, "Human Resources"),
    OTHER(8, "Other");
    
    private final int value;
    private final String displayName;
    
    Category(int value, String displayName) {
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
    public static Category fromValue(int value) {
        for (Category category : Category.values()) {
            if (category.getValue() == value) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid Category value: " + value);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
