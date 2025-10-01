package com.roze.nexacommerce.user.repository;

import com.roze.nexacommerce.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByName(String superadmin);

    Optional<Role> findByName(String superadmin);

    @Query("SELECT r FROM Role r WHERE r.name LIKE %:searchTerm% OR r.description LIKE %:searchTerm%")
    Page<Role> searchRoles(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    Set<Role> findByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT r FROM Role r WHERE r.name IN :roleNames")
    Set<Role> findByNames(@Param("roleNames") Set<String> roleNames);
}
