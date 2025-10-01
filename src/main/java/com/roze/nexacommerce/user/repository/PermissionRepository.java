package com.roze.nexacommerce.user.repository;

import com.roze.nexacommerce.user.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    boolean existsByName(String permissionName);

    Optional<Permission> findByName(String name);

    @Query("SELECT p FROM Permission p LEFT JOIN FETCH p.roles WHERE p.name IN :names")
    Set<Permission> findByNameIn(@Param("names") List<String> names);

    Set<Permission> findByIdIn(Set<Long> ids);

    @Query("SELECT p FROM Permission p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    Page<Permission> searchPermissions(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    Set<Permission> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT p FROM Permission p LEFT JOIN FETCH p.roles")
    Set<Permission> findAllWithRoles();

}
