package com.example.spring_boot_project_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.response.AttachmentUploadResponse;
import com.example.spring_boot_project_api.service.AttachmentUploadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/tour-place-attachments")
@RequiredArgsConstructor
public class TourPlaceAttachmentController {

    private final AttachmentUploadService attachmentUploadService;

    @GetMapping("/tour-place/{tourPlaceId}")
    public ResponseEntity<List<AttachmentUploadResponse>> findByTourPlace(@PathVariable Long tourPlaceId) {
        return ResponseEntity.ok(attachmentUploadService.findByTouristPlaceId(tourPlaceId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<AttachmentUploadResponse>> upload(
            @RequestParam("tourPlaceId") Long tourPlaceId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "type", required = false) String type) {
        if (files != null && !files.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(attachmentUploadService.uploadMultipleForTouristPlace(tourPlaceId, files, type));
        }
        AttachmentUploadResponse single = attachmentUploadService.uploadForTouristPlace(tourPlaceId, file, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(single));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long attachmentId) {
        attachmentUploadService.deleteForTouristPlace(attachmentId);
        return ResponseEntity.noContent().build();
    }
}