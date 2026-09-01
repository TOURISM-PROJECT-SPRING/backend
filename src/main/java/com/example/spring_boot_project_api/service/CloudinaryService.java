package com.example.spring_boot_project_api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.example.spring_boot_project_api.enums.AttachmentFileType;
import com.example.spring_boot_project_api.exception.CloudinaryDeleteException;
import com.example.spring_boot_project_api.exception.CloudinaryUploadException;

/**
 * Thin wrapper around the Cloudinary uploader. Credentials come from
 * environment variables via {@code application.properties}
 * ({@code cloudinary.cloud-name/api-key/api-secret}) and are never hardcoded.
 *
 * <p>Attachments are organised under a shared root folder:
 * {@code smart-tourism/{entity}/{entityId}/{publicId}} where {@code publicId}
 * is a generated {@link UUID}. The Cloudinary {@code public_id} is persisted so
 * the image can later be deleted from Cloudinary.</p>
 */
@Service
public class CloudinaryService {

    public static final String ROOT_FOLDER = "smart-tourism";

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    /**
     * Uploads a file and returns only its {@code secure_url}.
     *
     * <p>Kept for entities that store a plain image URL (e.g.
     * {@code TourPlaceServiceImpl}, {@code FoodServiceImpl},
     * {@code PlaceCategoryController}). The attachment system should use
     * {@link #uploadImage(MultipartFile, String, AttachmentFileType)} instead.</p>
     */
    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Uploads an image/document under {@code smart-tourism/{baseFolder}/{uuid}}.
     *
     * @param file     the multipart file to upload (must already be validated)
     * @param baseFolder entity-relative folder, e.g. {@code hotels/10}
     * @param fileType   IMAGE uploads use {@code resource_type=image},
     *                   DOCUMENT uploads use {@code resource_type=raw}
     * @return the Cloudinary upload result (secure_url + public_id + resource_type)
     * @throws CloudinaryUploadException if Cloudinary rejects the upload
     */
    public UploadResult uploadImage(MultipartFile file, String baseFolder, AttachmentFileType fileType) {
        String resourceType = (fileType == AttachmentFileType.DOCUMENT) ? "raw" : "image";
        String publicId = ROOT_FOLDER + "/" + baseFolder + "/" + UUID.randomUUID();
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", resourceType));
            return new UploadResult(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString(),
                    resourceType);
        } catch (RuntimeException | IOException e) {
            throw new CloudinaryUploadException(
                    "Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an image from Cloudinary using its {@code public_id}. No-op when
     * the public id is blank. Safe to call for assets whose
     * {@code resource_type} is {@code image} (the default).
     */
    public void delete(String publicId) {
        delete(publicId, "image");
    }

    /**
     * Deletes an image/document from Cloudinary using its {@code public_id} and
     * the stored {@code resource_type} ({@code image} or {@code raw}).
     */
    public void delete(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        String type = (resourceType == null || resourceType.isBlank()) ? "image" : resourceType;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", type,
                    "invalidate", true));
        } catch (RuntimeException | IOException e) {
            throw new CloudinaryDeleteException(
                    "Failed to delete file from Cloudinary (" + publicId + "): " + e.getMessage(), e);
        }
    }

    /** Result of a Cloudinary upload: the public URL, public id and resource type. */
    public record UploadResult(String secureUrl, String publicId, String resourceType) {
    }
}