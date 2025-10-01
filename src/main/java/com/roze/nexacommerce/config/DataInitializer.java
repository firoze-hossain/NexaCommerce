package com.roze.nexacommerce.config;

import com.roze.nexacommerce.user.entity.Permission;
import com.roze.nexacommerce.user.entity.Role;
import com.roze.nexacommerce.user.entity.User;
import com.roze.nexacommerce.user.repository.PermissionRepository;
import com.roze.nexacommerce.user.repository.RoleRepository;
import com.roze.nexacommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        createPermissions();
        createRoles();
        createSuperAdmin();
        createDefaultAdmin();
        log.info("Data initialization completed successfully");
    }

    private void createPermissions() {
        log.info("Creating permissions....");
        List<String> permissions = Arrays.asList(
                // User management permissions
                "CREATE_USER", "READ_USER", "UPDATE_USER", "DELETE_USER",
                "DEACTIVATE_USER", "ACTIVATE_USER",

                "CREATE_ADDRESS", "READ_ADDRESS", "UPDATE_ADDRESS", "DELETE_ADDRESS",
                // Customer management permissions - ADD THESE
                "CREATE_CUSTOMER", "READ_CUSTOMER", "UPDATE_CUSTOMER", "DELETE_CUSTOMER",
                // Role management permissions
                "CREATE_ROLE", "READ_ROLE", "UPDATE_ROLE", "DELETE_ROLE",

                // Permission management
                "CREATE_PERMISSION", "READ_PERMISSION", "UPDATE_PERMISSION", "DELETE_PERMISSION",

                // Vendor management
                "CREATE_VENDOR", "READ_VENDOR", "UPDATE_VENDOR", "DELETE_VENDOR",
                "APPROVE_VENDOR", "MANAGE_VENDORS",

                // Product management
                "CREATE_PRODUCT", "READ_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT",
                "MANAGE_PRODUCTS", "MANAGE_INVENTORY",

                // Category management
                "CREATE_CATEGORY", "READ_CATEGORY", "UPDATE_CATEGORY", "DELETE_CATEGORY",
                "MANAGE_CATEGORIES",

                // Order management
                "CREATE_ORDER", "READ_ORDER", "UPDATE_ORDER", "DELETE_ORDER",
                "MANAGE_ORDERS", "PROCESS_ORDERS",

                // Payment management
                "PROCESS_PAYMENT", "REFUND_PAYMENT", "VIEW_PAYMENTS", "MANAGE_PAYMENTS",

                // Review management
                "CREATE_REVIEW", "READ_REVIEW", "UPDATE_REVIEW", "DELETE_REVIEW",
                "MODERATE_REVIEWS",

                // Content management
                "MANAGE_CONTENT", "MANAGE_BANNERS", "MANAGE_COUPONS", "MANAGE_PROMOTIONS",

                // System management
                "VIEW_REPORTS", "MANAGE_SETTINGS", "VIEW_ANALYTICS", "EXPORT_DATA",

                // Vendor-specific permissions
                "MANAGE_MY_PRODUCTS", "MANAGE_MY_ORDERS", "VIEW_MY_REPORTS", "MANAGE_MY_INVENTORY"
        );
        int createdCount = 0;
        for (String permissionName : permissions) {
            if (!permissionRepository.existsByName(permissionName)) {
                Permission permission = Permission.builder()
                        .name(permissionName)
                        .description(generatePermissionDescription(permissionName))
                        .build();
                permissionRepository.save(permission);
                createdCount++;
                log.debug("Created permission:{}", permissionName);
            }
        }
        log.info("Permissions created completed.Created {} new permissions.", createdCount);
    }

    private String generatePermissionDescription(String permissionName) {
        String action = permissionName.split("_")[0].toLowerCase();
        String resource = permissionName.substring(permissionName.indexOf('_') + 1).toLowerCase().replace("_", " ");
        switch (action) {
            case "create":
                return "Allows creating new " + resource;
            case "read":
                return "Allows viewing " + resource;
            case "update":
                return "Allows modifying " + resource;
            case "delete":
                return "Allows deleting " + resource;
            case "manage":
                return "Allows full management of " + resource;
            case "view":
                return "Allows viewing " + resource;
            case "process":
                return "Allows processing " + resource;
            case "approve":
                return "Allows approving " + resource;
            case "moderate":
                return "Allows moderating " + resource;
            case "export":
                return "Allows exporting " + resource;
            case "deactivate":
                return "Allows deactivating " + resource;
            case "activate":
                return "Allows activating " + resource;
            case "refund":
                return "Allows refunding " + resource;
            default:
                return "Permission for " + action + " " + resource;
        }
    }

    private void createRoles() {
        log.info("Creating roles...");
        // SUPERADMIN - All permissions
        if (!roleRepository.existsByName("SUPERADMIN")) {
            Role superAdmin = Role.builder()
                    .name("SUPERADMIN")
                    .description("Fully system control with all permissions")
                    .permissions(new HashSet<>(permissionRepository.findAll()))
                    .build();
            roleRepository.save(superAdmin);
            log.info("Created SUPERADMIN role with all permissions");
        }
        // ADMIN - Most administrative permissions
        if (!roleRepository.existsByName("ADMIN")) {
            Set<Permission> adminPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    // User management
                    "CREATE_USER", "READ_USER", "UPDATE_USER", "DEACTIVATE_USER", "ACTIVATE_USER",
                    // Customer management - ADD THESE
                    "READ_CUSTOMER", "UPDATE_CUSTOMER", "DELETE_CUSTOMER",
                    // Vendor management
                    "CREATE_VENDOR", "READ_VENDOR", "UPDATE_VENDOR", "DELETE_VENDOR",
                    "APPROVE_VENDOR", "MANAGE_VENDORS",

                    // Product management
                    "CREATE_PRODUCT", "READ_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT",
                    "MANAGE_PRODUCTS", "MANAGE_CATEGORIES",

                    // Order management
                    "READ_ORDER", "UPDATE_ORDER", "MANAGE_ORDERS", "PROCESS_ORDERS",

                    // Payment management
                    "VIEW_PAYMENTS", "PROCESS_PAYMENT", "REFUND_PAYMENT",

                    // Review management
                    "READ_REVIEW", "DELETE_REVIEW", "MODERATE_REVIEWS",

                    // Content management
                    "MANAGE_CONTENT", "MANAGE_BANNERS", "MANAGE_COUPONS", "MANAGE_PROMOTIONS",

                    // System management
                    "VIEW_REPORTS", "VIEW_ANALYTICS", "EXPORT_DATA"

            ));
            Role admin = Role.builder()
                    .name("ADMIN")
                    .description("Administrative access to manage platform operations")
                    .permissions(adminPermissions)
                    .build();
            roleRepository.save(admin);
            log.info("Created ADMIN role with {} permissions", adminPermissions.size());
        }
        // EDITOR - Content management permissions
        if (!roleRepository.existsByName("EDITOR")) {
            Set<Permission> editorPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "MANAGE_CONTENT", "MANAGE_BANNERS", "MANAGE_COUPONS", "MANAGE_PROMOTIONS",
                    "READ_PRODUCT", "READ_CATEGORY", "READ_USER", "READ_VENDOR"
            ));
            Role editor = Role.builder()
                    .name("EDITOR")
                    .description("Manage content, banners, promotions and marketing")
                    .permissions(editorPermissions)
                    .build();
            roleRepository.save(editor);
            log.info("Created EDITOR role with {} permissions", editorPermissions.size());
        }
        // MODERATOR - Review and content moderation
        if (!roleRepository.existsByName("MODERATOR")) {
            Set<Permission> moderatorPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "READ_REVIEW", "UPDATE_REVIEW", "DELETE_REVIEW", "MODERATE_REVIEWS",
                    "READ_USER", "READ_PRODUCT", "READ_ORDER"
            ));

            Role moderator = Role.builder()
                    .name("MODERATOR")
                    .description("Moderate reviews and handle user disputes")
                    .permissions(moderatorPermissions)
                    .build();
            roleRepository.save(moderator);
            log.info("Created MODERATOR role with {} permissions", moderatorPermissions.size());
        }

        // VENDOR - Vendor specific permissions
        if (!roleRepository.existsByName("VENDOR")) {
            Set<Permission> vendorPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "MANAGE_MY_PRODUCTS", "MANAGE_MY_ORDERS", "VIEW_MY_REPORTS", "MANAGE_MY_INVENTORY",
                    "CREATE_PRODUCT", "READ_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT",
                    "READ_ORDER", "UPDATE_ORDER", "VIEW_ANALYTICS"
            ));

            Role vendor = Role.builder()
                    .name("VENDOR")
                    .description("Manage their own products, orders and inventory")
                    .permissions(vendorPermissions)
                    .build();
            roleRepository.save(vendor);
            log.info("Created VENDOR role with {} permissions", vendorPermissions.size());
        }
        // CUSTOMER - Basic permissions
        if (!roleRepository.existsByName("CUSTOMER")) {
            Set<Permission> customerPermissions = permissionRepository.findByNameIn(Arrays.asList(
                    "READ_CUSTOMER",
                    "READ_PRODUCT", "READ_CATEGORY", "CREATE_ORDER", "READ_ORDER",
                    "CREATE_REVIEW", "READ_REVIEW", "UPDATE_REVIEW"
            ));

            Role customer = Role.builder()
                    .name("CUSTOMER")
                    .description("Browse products, place orders and write reviews")
                    .permissions(customerPermissions)
                    .build();
            roleRepository.save(customer);
            log.info("Created CUSTOMER role with {} permissions", customerPermissions.size());
        }
    }
    private void createSuperAdmin() {
        String superAdminEmail = "superadmin@nextcommerce.com";
        if (!userRepository.existsByEmail(superAdminEmail)) {
            Role superAdminRole = roleRepository.findByName("SUPERADMIN")
                    .orElseThrow(() -> new RuntimeException("SUPERADMIN role not found"));

            User superAdmin = User.builder()
                    .name("Super Administrator")
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode("SuperAdmin123!"))
                    .role(superAdminRole)
                    .active(true)
                    .build();

            userRepository.save(superAdmin);
            log.info("Created SUPERADMIN user: {}", superAdminEmail);
        } else {
            log.info("SUPERADMIN user already exists: {}", superAdminEmail);
        }
    }

    private void createDefaultAdmin() {
        String adminEmail = "admin@nextcommerce.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            User admin = User.builder()
                    .name("System Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin123!"))
                    .role(adminRole)
                    .active(true)
                    .build();

            userRepository.save(admin);
            log.info("Created default ADMIN user: {}", adminEmail);
        } else {
            log.info("Default ADMIN user already exists: {}", adminEmail);
        }
    }

    private void createSampleVendor() {
        String vendorEmail = "vendor@example.com";
        if (!userRepository.existsByEmail(vendorEmail)) {
            Role vendorRole = roleRepository.findByName("VENDOR")
                    .orElseThrow(() -> new RuntimeException("VENDOR role not found"));

            User vendor = User.builder()
                    .name("Sample Vendor")
                    .email(vendorEmail)
                    .password(passwordEncoder.encode("Vendor123!"))
                    .role(vendorRole)
                    .active(true)
                    .build();

            userRepository.save(vendor);
            log.info("Created sample VENDOR user: {}", vendorEmail);
        }
    }

    private void createSampleCustomer() {
        String customerEmail = "customer@example.com";
        if (!userRepository.existsByEmail(customerEmail)) {
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));

            User customer = User.builder()
                    .name("Sample Customer")
                    .email(customerEmail)
                    .password(passwordEncoder.encode("Customer123!"))
                    .role(customerRole)
                    .active(true)
                    .build();

            userRepository.save(customer);
            log.info("Created sample CUSTOMER user: {}", customerEmail);
        }
    }

}
