package com.example.spring_boot_project_api.service;

import java.util.List;

import com.example.spring_boot_project_api.dto.request.ContactMessageRequest;
import com.example.spring_boot_project_api.dto.response.ContactMessageResponse;

public interface ContactMessageService {

    List<ContactMessageResponse> findAll();

    ContactMessageResponse findById(Long id);

    ContactMessageResponse create(ContactMessageRequest request);

    ContactMessageResponse markRead(Long id);

    void delete(Long id);
}