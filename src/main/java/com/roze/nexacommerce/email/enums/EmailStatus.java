package com.roze.nexacommerce.email.enums;

public enum EmailStatus {
    PENDING("Pending", "Email is queued for sending"),
    SENT("Sent", "Email successfully sent"),
    DELIVERED("Delivered", "Email delivered to recipient"),
    FAILED("Failed", "Email sending failed"),
    BOUNCED("Bounced", "Email bounced back"),
    OPENED("Opened", "Email opened by recipient"),
    CLICKED("Clicked", "Link in email clicked"),
    COMPLAINED("Complained", "Recipient marked as spam");

    private final String displayName;
    private final String description;

    EmailStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}