package com.roze.nexacommerce.email.repository;

import com.roze.nexacommerce.email.entity.EmailTemplate;
import com.roze.nexacommerce.email.enums.EmailCategory;
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
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    // Find active template by key
    Optional<EmailTemplate> findByTemplateKeyAndIsActiveTrue(String templateKey);

    // Find template by key (regardless of active status)
    Optional<EmailTemplate> findByTemplateKey(String templateKey);

    // Find templates by category
    List<EmailTemplate> findByCategoryAndIsActiveTrue(EmailCategory category);

    // Find all active templates
    List<EmailTemplate> findByIsActiveTrue();

    // Find system templates
    List<EmailTemplate> findByIsSystemTemplateTrueAndIsActiveTrue();

    // Find templates by language
    List<EmailTemplate> findByLanguageAndIsActiveTrue(String language);

    // Check if template key exists
    boolean existsByTemplateKey(String templateKey);

    // Find template with specific language
    @Query("SELECT et FROM EmailTemplate et WHERE et.isActive = true AND et.templateKey = :templateKey AND et.language = :language")
    Optional<EmailTemplate> findByTemplateKeyAndLanguage(@Param("templateKey") String templateKey, @Param("language") String language);

    // Find default template for a category
    @Query("SELECT et FROM EmailTemplate et WHERE et.isActive = true AND et.category = :category ORDER BY et.isSystemTemplate DESC, et.templateVersion DESC")
    List<EmailTemplate> findDefaultTemplatesByCategory(@Param("category") EmailCategory category);

    // Paginated queries
    Page<EmailTemplate> findByIsActiveTrue(Pageable pageable);
    Page<EmailTemplate> findByCategory(EmailCategory category, Pageable pageable);
    Page<EmailTemplate> findByIsSystemTemplate(Boolean isSystemTemplate, Pageable pageable);

    // Find templates by version range
    @Query("SELECT et FROM EmailTemplate et WHERE et.isActive = true AND et.templateVersion BETWEEN :minVersion AND :maxVersion")
    List<EmailTemplate> findTemplatesByVersionRange(@Param("minVersion") Integer minVersion, @Param("maxVersion") Integer maxVersion);

    // Find latest version of a template
    @Query("SELECT et FROM EmailTemplate et WHERE et.templateKey = :templateKey AND et.isActive = true ORDER BY et.version DESC")
    List<EmailTemplate> findLatestVersionByTemplateKey(@Param("templateKey") String templateKey);

    // Count templates by category
    @Query("SELECT COUNT(et) FROM EmailTemplate et WHERE et.isActive = true AND et.category = :category")
    Long countActiveByCategory(@Param("category") EmailCategory category);

    // Deactivate old versions when creating new one
    @Modifying
    @Query("UPDATE EmailTemplate et SET et.isActive = false WHERE et.templateKey = :templateKey AND et.templateVersion < :templateVersion")
    void deactivateOldTemplateVersions(@Param("templateKey") String templateKey, @Param("templateVersion") Integer templateVersion);

    // Increment version for a template
    @Modifying
    @Query("UPDATE EmailTemplate et SET et.templateVersion = et.templateVersion + 1 WHERE et.templateKey = :templateKey")
    void incrementVersion(@Param("templateKey") String templateKey);

    // Find templates that support a specific purpose (using JSON field)
    @Query(value = "SELECT * FROM email_templates et WHERE et.is_active = true AND JSON_CONTAINS(et.supported_purposes, :purpose, '$')", nativeQuery = true)
    List<EmailTemplate> findTemplatesSupportingPurpose(@Param("purpose") String purpose);
}