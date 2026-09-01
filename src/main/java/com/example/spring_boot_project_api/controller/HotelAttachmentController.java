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
@RequestMapping("api/hotels/{hotelId}/attachments")
@RequiredArgsConstructor
public class HotelAttachmentController {

    private final AttachmentUploadService attachmentUploadService;

    @GetMapping
    public ResponseEntity<List<AttachmentUploadResponse>> findByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(attachmentUploadService.findByHotelId(hotelId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<AttachmentUploadResponse>> upload(
            @PathVariable Long hotelId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "type", required = false) String type) {
        if (files != null && !files.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(attachmentUploadService.uploadMultipleForHotel(hotelId, files, type));
        }
        AttachmentUploadResponse single = attachmentUploadService.uploadForHotel(hotelId, file, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(single));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long hotelId,
                                       @PathVariable Long attachmentId) {
        attachmentUploadService.deleteForHotel(hotelId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
