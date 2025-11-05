package com.roze.nexacommerce.email.repository;

import com.roze.nexacommerce.email.entity.EmailAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailAttachmentRepository extends JpaRepository<EmailAttachment, Long> {
    
    // Find attachments by email log
    List<EmailAttachment> findByEmailLogId(Long emailLogId);
    
    // Find inline attachments
    List<EmailAttachment> findByEmailLogIdAndIsInlineTrue(Long emailLogId);
    
    // Find attachments by content ID (for inline images)
    Optional<EmailAttachment> findByContentId(String contentId);
    
    // Find attachments by file type
    List<EmailAttachment> findByFileType(String fileType);
    
    // Count attachments by email log
    Long countByEmailLogId(Long emailLogId);
    
    // Delete attachments by email log
    @Modifying
    @Query("DELETE FROM EmailAttachment ea WHERE ea.emailLog.id = :emailLogId")
    void deleteByEmailLogId(@Param("emailLogId") Long emailLogId);
    
    // Find large attachments
    @Query("SELECT ea FROM EmailAttachment ea WHERE ea.fileSize > :minSize")
    List<EmailAttachment> findLargeAttachments(@Param("minSize") Long minSize);
}