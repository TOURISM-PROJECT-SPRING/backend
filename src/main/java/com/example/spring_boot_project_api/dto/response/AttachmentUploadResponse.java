package com.example.spring_boot_project_api.dto.response;

import java.time.LocalDateTime;

import com.example.spring_boot_project_api.enums.AttachmentFileType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AttachmentUploadResponse {

    private Long id;

    private String fileName;

    private String originalName;

    private AttachmentFileType fileType;

    private String mimeType;

    private Long fileSize;

    private String cloudinaryUrl;

    private String type;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}