package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.model.UserRoles;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoles, Long> {

    List<UserRoles> findByUserId(Long userId);

    List<UserRoles> findByRoleId(Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    @Transactional
    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    @Transactional
    void deleteByUserId(Long userId);
}
