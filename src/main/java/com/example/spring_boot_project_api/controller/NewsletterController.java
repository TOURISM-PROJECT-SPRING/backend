package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.NewsletterSubscribeRequest;
import com.example.spring_boot_project_api.dto.response.NewsletterSubscriberResponse;
import com.example.spring_boot_project_api.service.NewsletterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping
    public ResponseEntity<List<NewsletterSubscriberResponse>> getAllSubscribers() {
        return ResponseEntity.ok(newsletterService.findAll());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterSubscriberResponse> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsletterService.subscribe(request));
    }
}