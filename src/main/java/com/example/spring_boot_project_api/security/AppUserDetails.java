package com.example.spring_boot_project_api.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.spring_boot_project_api.model.Users;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public static AppUserDetails from(Users user) {
        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .map(ur -> ur.getRole() != null ? ur.getRole().getName() : null)
                .filter(name -> name != null)
                .map(name -> new SimpleGrantedAuthority("ROLE_" + name.toUpperCase()))
                .collect(Collectors.toList());

        return new AppUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities);
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
        return true;
    }
}