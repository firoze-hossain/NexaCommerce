package com.roze.nexacommerce.email.enums;

public enum EmailPriority {
    LOW("Low", "Non-urgent emails"),
    NORMAL("Normal", "Standard priority emails"),
    HIGH("High", "Important emails"),
    URGENT("Urgent", "Time-sensitive critical emails");

    private final String displayName;
    private final String description;

    EmailPriority(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}