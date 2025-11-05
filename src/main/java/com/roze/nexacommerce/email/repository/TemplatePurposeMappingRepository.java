package com.roze.nexacommerce.email.repository;

import com.roze.nexacommerce.email.entity.TemplatePurposeMapping;
import com.roze.nexacommerce.email.enums.EmailPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplatePurposeMappingRepository extends JpaRepository<TemplatePurposeMapping, Long> {
    
    // Find mappings by template
    List<TemplatePurposeMapping> findByEmailTemplateId(Long templateId);
    
    // Find mappings by purpose
    List<TemplatePurposeMapping> findByPurpose(EmailPurpose purpose);
    
    // Find default template for a purpose
    Optional<TemplatePurposeMapping> findByPurposeAndIsDefaultTrue(EmailPurpose purpose);
    
    // Find specific mapping
    Optional<TemplatePurposeMapping> findByEmailTemplateIdAndPurpose(Long templateId, EmailPurpose purpose);
    
    // Check if default exists for purpose
    boolean existsByPurposeAndIsDefaultTrue(EmailPurpose purpose);
    
    // Find all default mappings
    List<TemplatePurposeMapping> findByIsDefaultTrue();
    
    // Find mappings with priority
    List<TemplatePurposeMapping> findByPurposeOrderByPriorityAsc(EmailPurpose purpose);
    
    // Custom query to find best template for purpose
    @Query("SELECT tpm FROM TemplatePurposeMapping tpm WHERE tpm.purpose = :purpose ORDER BY tpm.isDefault DESC, tpm.priority ASC")
    List<TemplatePurposeMapping> findBestTemplateForPurpose(@Param("purpose") EmailPurpose purpose);
    
    // Remove all mappings for a template
    @Modifying
    @Query("DELETE FROM TemplatePurposeMapping tpm WHERE tpm.emailTemplate.id = :templateId")
    void deleteByTemplateId(@Param("templateId") Long templateId);
    
    // Update default flag
    @Modifying
    @Query("UPDATE TemplatePurposeMapping tpm SET tpm.isDefault = CASE WHEN tpm.id = :mappingId THEN true ELSE false END WHERE tpm.purpose = :purpose")
    void updateDefaultMapping(@Param("purpose") EmailPurpose purpose, @Param("mappingId") Long mappingId);
    
    // Find templates that support multiple purposes
    @Query("SELECT tpm FROM TemplatePurposeMapping tpm WHERE tpm.purpose IN :purposes")
    List<TemplatePurposeMapping> findByPurposes(@Param("purposes") List<EmailPurpose> purposes);
}