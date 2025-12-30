// ReturnPolicyRepository.java
package com.roze.nexacommerce.returns.repository;

import com.roze.nexacommerce.returns.entity.ReturnPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnPolicyRepository extends JpaRepository<ReturnPolicy, Long> {
    @Query("SELECT p FROM ReturnPolicy p WHERE p.defaultPolicy = true")
    Optional<ReturnPolicy> findDefaultPolicy();
    
    List<ReturnPolicy> findByActiveTrue();
    
    @Query("SELECT p FROM ReturnPolicy p WHERE p.active = true ORDER BY p.defaultPolicy DESC, p.name ASC")
    List<ReturnPolicy> findAllActivePolicies();
    
    boolean existsByName(String name);
    
    @Query("SELECT p FROM ReturnPolicy p WHERE p.active = true AND p.defaultPolicy = true")
    Optional<ReturnPolicy> findActiveDefaultPolicy();
    
    @Query("SELECT COUNT(p) > 0 FROM ReturnPolicy p WHERE p.name = :name AND p.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}