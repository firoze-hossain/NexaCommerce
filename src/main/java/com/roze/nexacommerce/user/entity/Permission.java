package com.roze.nexacommerce.user.entity;

import com.roze.nexacommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
}
