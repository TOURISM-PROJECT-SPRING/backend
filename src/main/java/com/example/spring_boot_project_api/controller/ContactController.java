package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_project_api.dto.request.ContactMessageRequest;
import com.example.spring_boot_project_api.dto.response.ContactMessageResponse;
import com.example.spring_boot_project_api.service.ContactMessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageService contactMessageService;

    @GetMapping
    public ResponseEntity<List<ContactMessageResponse>> getAllMessages() {
        return ResponseEntity.ok(contactMessageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactMessageResponse> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ContactMessageResponse> createMessage(
            @Valid @RequestBody ContactMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactMessageService.create(request));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ContactMessageResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.markRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        contactMessageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}