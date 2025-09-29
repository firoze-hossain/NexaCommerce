package com.roze.nexacommerce.user.repository;

import com.roze.nexacommerce.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByName(String superadmin);

    Optional<Role> findByName(String superadmin);
}
