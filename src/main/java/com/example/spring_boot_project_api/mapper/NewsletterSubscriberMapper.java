package com.example.spring_boot_project_api.mapper;

import com.example.spring_boot_project_api.dto.response.NewsletterSubscriberResponse;
import com.example.spring_boot_project_api.model.NewsletterSubscribers;

public class NewsletterSubscriberMapper {

    private NewsletterSubscriberMapper() {}

    public static NewsletterSubscriberResponse toResponse(NewsletterSubscribers subscriber) {
        if (subscriber == null) return null;
        return NewsletterSubscriberResponse.builder()
                .id(subscriber.getId())
                .email(subscriber.getEmail())
                .createdAt(subscriber.getCreatedAt())
                .build();
    }
}