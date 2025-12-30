package com.roze.nexacommerce.user.entity;

import com.roze.nexacommerce.common.BaseEntity;
import com.roze.nexacommerce.customer.entity.CustomerProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails, UserPrincipal {
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String phone;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Add bidirectional relationship with CustomerProfile
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private CustomerProfile customerProfile;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = role.getPermissions().stream().map(permission ->
                new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority(role.getName()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public String getName() {
        return name;
    }

    public Long getUserId() {
        return this.getId();
    }

    public boolean hasRole(String roleName) {
        return this.role != null && this.role.getName().equals(roleName);
    }

    public boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    public boolean isVendor() {
        return hasRole("VENDOR");
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPERADMIN");
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}
