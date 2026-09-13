package com.example.spring_boot_project_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.NewsletterSubscribers;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscribers, Long> {

    Optional<NewsletterSubscribers> findByEmail(String email);

    boolean existsByEmail(String email);
}