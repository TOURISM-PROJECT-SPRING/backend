package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.NewsletterSubscribeRequest;
import com.example.spring_boot_project_api.dto.response.NewsletterSubscriberResponse;
import com.example.spring_boot_project_api.mapper.NewsletterSubscriberMapper;
import com.example.spring_boot_project_api.model.NewsletterSubscribers;
import com.example.spring_boot_project_api.repository.NewsletterSubscriberRepository;
import com.example.spring_boot_project_api.service.NewsletterService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsletterServiceImpl implements NewsletterService {

    private final NewsletterSubscriberRepository newsletterSubscriberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NewsletterSubscriberResponse> findAll() {
        return newsletterSubscriberRepository.findAll().stream()
                .map(NewsletterSubscriberMapper::toResponse)
                .toList();
    }

    @Override
    public NewsletterSubscriberResponse subscribe(NewsletterSubscribeRequest request) {
        NewsletterSubscriberResponse existing = null;
        if (newsletterSubscriberRepository.existsByEmail(request.getEmail())) {
            existing = NewsletterSubscriberMapper.toResponse(
                    newsletterSubscriberRepository.findByEmail(request.getEmail()).orElse(null));
            return existing;
        }

        NewsletterSubscribers subscriber = new NewsletterSubscribers();
        subscriber.setEmail(request.getEmail());
        NewsletterSubscribers saved = newsletterSubscriberRepository.save(subscriber);
        return NewsletterSubscriberMapper.toResponse(saved);
    }
}