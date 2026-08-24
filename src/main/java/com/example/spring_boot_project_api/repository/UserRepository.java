package com.example.spring_boot_project_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.enums.UserEnum;
import com.example.spring_boot_project_api.model.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    
    // for find user by username
    Optional<Users> findByUsername(String username);

    // for find user by email
    Optional<Users> findByEmail(String email);

    // check username have already or not
    boolean existsByUsername(String username);

    // check email have already or not
    boolean existsByEmail(String email);

    // find by status
    List<Users> findByStatus(UserEnum status);
}
