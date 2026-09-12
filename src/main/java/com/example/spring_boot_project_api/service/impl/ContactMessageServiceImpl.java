package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.ContactMessageRequest;
import com.example.spring_boot_project_api.dto.response.ContactMessageResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.ContactMessageMapper;
import com.example.spring_boot_project_api.model.ContactMessages;
import com.example.spring_boot_project_api.repository.ContactMessageRepository;
import com.example.spring_boot_project_api.service.ContactMessageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessageResponse> findAll() {
        return ContactMessageMapper.toResponseList(
                contactMessageRepository.findByOrderByCreatedAtDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public ContactMessageResponse findById(Long id) {
        ContactMessages message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message", id));
        return ContactMessageMapper.toResponse(message);
    }

    @Override
    public ContactMessageResponse create(ContactMessageRequest request) {
        ContactMessages message = ContactMessageMapper.toEntity(request);
        ContactMessages saved = contactMessageRepository.save(message);
        return ContactMessageMapper.toResponse(saved);
    }

    @Override
    public ContactMessageResponse markRead(Long id) {
        ContactMessages message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message", id));
        message.setIsRead(Boolean.TRUE);
        ContactMessages saved = contactMessageRepository.save(message);
        return ContactMessageMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contact message", id);
        }
        contactMessageRepository.deleteById(id);
    }
}