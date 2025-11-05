package com.roze.nexacommerce.email.enums;

public enum EmailCategory {
    TRANSACTIONAL("Transactional", "Emails triggered by user actions and transactions"),
    MARKETING("Marketing", "Promotional and marketing communications"),
    NOTIFICATION("Notification", "System and account notifications"),
    SUPPORT("Support", "Customer service and support communications"),
    SECURITY("Security", "Security-related alerts and verifications"),
    ADMIN("Administrative", "Internal administrative communications"),
    VENDOR("Vendor", "Vendor-specific communications");

    private final String displayName;
    private final String description;

    EmailCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}