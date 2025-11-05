package com.roze.nexacommerce.email.enums;

public enum EmailProvider {
    SMTP("SMTP"),
    SENDGRID("SendGrid"),
    MAILGUN("Mailgun"),
    AMAZON_SES("Amazon SES"),
    GMAIL("Gmail API"),
    OUTLOOK("Outlook API"),
    CUSTOM("Custom API");

    private final String displayName;

    EmailProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}