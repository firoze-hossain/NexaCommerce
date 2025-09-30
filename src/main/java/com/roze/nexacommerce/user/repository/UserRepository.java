package com.roze.nexacommerce.user.repository;

import com.roze.nexacommerce.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String superAdminEmail);

    @Query("select u from User u where u.role.name=:roleName")
    Page<User> findByRoleName(String roleName, Pageable pageable);
}
