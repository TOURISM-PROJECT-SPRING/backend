package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.NewsletterSubscribeRequest;
import com.example.spring_boot_project_api.dto.response.NewsletterSubscriberResponse;

public interface NewsletterService {

    List<NewsletterSubscriberResponse> findAll();

    NewsletterSubscriberResponse subscribe(NewsletterSubscribeRequest request);
}