package com.roze.nexacommerce.user.repository;

import com.roze.nexacommerce.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    boolean existsByName(String permissionName);

    Set<Permission> findByNameIn(List<String> asList);

}
