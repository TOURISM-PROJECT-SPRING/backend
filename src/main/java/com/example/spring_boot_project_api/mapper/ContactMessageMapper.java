package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.ContactMessageRequest;
import com.example.spring_boot_project_api.dto.response.ContactMessageResponse;
import com.example.spring_boot_project_api.model.ContactMessages;

public class ContactMessageMapper {

    private ContactMessageMapper() {}

    public static ContactMessages toEntity(ContactMessageRequest request) {
        ContactMessages message = new ContactMessages();
        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());
        message.setIsRead(Boolean.FALSE);
        return message;
    }

    public static ContactMessageResponse toResponse(ContactMessages message) {
        if (message == null) return null;
        return ContactMessageResponse.builder()
                .id(message.getId())
                .name(message.getName())
                .email(message.getEmail())
                .subject(message.getSubject())
                .message(message.getMessage())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }

    public static List<ContactMessageResponse> toResponseList(List<ContactMessages> messages) {
        if (messages == null) return Collections.emptyList();
        return messages.stream()
                .map(ContactMessageMapper::toResponse)
                .collect(Collectors.toList());
    }
}