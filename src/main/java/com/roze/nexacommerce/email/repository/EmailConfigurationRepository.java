package com.roze.nexacommerce.email.repository;

import com.roze.nexacommerce.email.entity.EmailConfiguration;
import com.roze.nexacommerce.email.enums.EmailProvider;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Long> {

    // Find active configuration by purpose
    Optional<EmailConfiguration> findByPurposeAndIsActiveTrue(EmailPurpose purpose);

    // Find default configuration
    Optional<EmailConfiguration> findByIsDefaultTrue();

    // Find all active configurations
    List<EmailConfiguration> findByIsActiveTrue();

    // Find configurations by provider
    List<EmailConfiguration> findByProviderAndIsActiveTrue(EmailProvider provider);

    // Check if purpose already has active configuration
    boolean existsByPurposeAndIsActiveTrue(EmailPurpose purpose);

    // Find configuration by purpose (regardless of active status)
    Optional<EmailConfiguration> findByPurpose(EmailPurpose purpose);

    // Paginated queries
    Page<EmailConfiguration> findByIsActiveTrue(Pageable pageable);
    Page<EmailConfiguration> findByProvider(EmailProvider provider, Pageable pageable);

    // Custom query with ordering
    @Query("SELECT ec FROM EmailConfiguration ec WHERE ec.isActive = true AND ec.purpose = :purpose ORDER BY ec.isDefault DESC, ec.createdAt DESC")
    Optional<EmailConfiguration> findActiveConfigurationByPurpose(@Param("purpose") EmailPurpose purpose);

    // Find configurations that support multiple purposes
    @Query("SELECT ec FROM EmailConfiguration ec WHERE ec.isActive = true AND ec.purpose IN :purposes")
    List<EmailConfiguration> findActiveConfigurationsByPurposes(@Param("purposes") List<EmailPurpose> purposes);

    // Count active configurations by provider
    @Query("SELECT COUNT(ec) FROM EmailConfiguration ec WHERE ec.isActive = true AND ec.provider = :provider")
    Long countActiveByProvider(@Param("provider") EmailProvider provider);

    // Deactivate all configurations (useful when setting new default)
    @Modifying
    @Query("UPDATE EmailConfiguration ec SET ec.isActive = false WHERE ec.isActive = true")
    void deactivateAllConfigurations();

    // Set a configuration as default and deactivate others for the same purpose
    @Modifying
    @Query("UPDATE EmailConfiguration ec SET ec.isDefault = CASE WHEN ec.id = :configId THEN true ELSE false END WHERE ec.purpose = :purpose")
    void updateDefaultConfiguration(@Param("purpose") EmailPurpose purpose, @Param("configId") Long configId);

    // Find configurations with rate limiting issues
    @Query("SELECT ec FROM EmailConfiguration ec WHERE ec.rateLimitPerHour > 0 AND ec.isActive = true")
    List<EmailConfiguration> findConfigurationsWithRateLimiting();
}