package com.roze.nexacommerce.email.enums;

public enum EmailPurpose {
    // Customer Communications
    CUSTOMER_SUPPORT("Customer Support", "customer-support", "Emails for customer support inquiries"),
    CONTACT_US("Contact Us", "contact", "Contact form submissions"),
    HELP_DESK("Help Desk", "help", "Help desk communications"),

    // Order & Transactional
    ORDER_CONFIRMATION("Order Confirmation", "order-confirmation", "Order confirmation emails"),
    ORDER_SHIPPED("Order Shipped", "order-shipped", "Shipping notifications"),
    ORDER_DELIVERED("Order Delivered", "order-delivered", "Delivery confirmations"),
    ORDER_CANCELLED("Order Cancelled", "order-cancelled", "Order cancellation notices"),
    PAYMENT_CONFIRMATION("Payment Confirmation", "payment-confirmation", "Payment success notifications"),
    PAYMENT_FAILED("Payment Failed", "payment-failed", "Payment failure alerts"),
    REFUND_PROCESSED("Refund Processed", "refund-processed", "Refund completion notices"),

    // Account Management
    ACCOUNT_VERIFICATION("Account Verification", "account-verification", "Email verification"),
    PASSWORD_RESET("Password Reset", "password-reset", "Password reset requests"),
    WELCOME_EMAIL("Welcome Email", "welcome", "New user welcome emails"),
    ACCOUNT_DELETED("Account Deleted", "account-deleted", "Account deletion confirmation"),

    // Marketing
    NEWSLETTER("Newsletter", "newsletter", "Regular newsletter distributions"),
    PROMOTIONAL("Promotional", "promotional", "Promotional offers and discounts"),
    ABANDONED_CART("Abandoned Cart", "abandoned-cart", "Cart abandonment reminders"),
    PRODUCT_RECOMMENDATIONS("Product Recommendations", "product-recommendations", "Personalized product suggestions"),

    // System & Admin
    SYSTEM_ALERTS("System Alerts", "system-alerts", "System monitoring alerts"),
    SECURITY_NOTIFICATIONS("Security Notifications", "security", "Security-related alerts"),
    ADMIN_NOTIFICATIONS("Admin Notifications", "admin", "Administrative notifications"),

    // Vendor Communications
    VENDOR_REGISTRATION("Vendor Registration", "vendor-registration", "Vendor registration emails"),
    VENDOR_ORDERS("Vendor Orders", "vendor-orders", "Vendor order notifications"),
    VENDOR_PAYOUTS("Vendor Payouts", "vendor-payouts", "Vendor payout information");

    private final String displayName;
    private final String slug;
    private final String description;

    EmailPurpose(String displayName, String slug, String description) {
        this.displayName = displayName;
        this.slug = slug;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
}