package com.example.spring_boot_project_api.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.response.AttachmentUploadResponse;

/**
 * File-upload attachment service. Each method uploads the physical file to
 * Cloudinary (via {@link CloudinaryService}), creates the {@code attachments}
 * metadata row and finally links it to the owning entity through the matching
 * junction table. PostgreSQL stores only metadata + the Cloudinary URL and
 * {@code public_id}.
 *
 * <p>All database work is transactional; if the transaction fails after the
 * Cloudinary upload, the uploaded image is deleted automatically so that no
 * orphaned Cloudinary images are left behind.</p>
 */
public interface AttachmentUploadService {

    // ---------------- User ----------------

    AttachmentUploadResponse uploadForUser(Long userId, MultipartFile file, String type);

    List<AttachmentUploadResponse> uploadMultipleForUser(Long userId, List<MultipartFile> files, String type);

    List<AttachmentUploadResponse> findByUserId(Long userId);

    void deleteForUser(Long userId, Long attachmentId);

    // ---------------- Hotel ----------------

    AttachmentUploadResponse uploadForHotel(Long hotelId, MultipartFile file, String type);

    List<AttachmentUploadResponse> uploadMultipleForHotel(Long hotelId, List<MultipartFile> files, String type);

    List<AttachmentUploadResponse> findByHotelId(Long hotelId);

    void deleteForHotel(Long hotelId, Long attachmentId);

    // ---------------- Room ----------------

    AttachmentUploadResponse uploadForRoom(Long roomId, MultipartFile file, String type);

    List<AttachmentUploadResponse> uploadMultipleForRoom(Long roomId, List<MultipartFile> files, String type);

    List<AttachmentUploadResponse> findByRoomId(Long roomId);

    void deleteForRoom(Long roomId, Long attachmentId);

    // ---------------- Tour place ----------------

    AttachmentUploadResponse uploadForTouristPlace(Long tourPlaceId, MultipartFile file, String type);

    List<AttachmentUploadResponse> uploadMultipleForTouristPlace(Long tourPlaceId, List<MultipartFile> files, String type);

    List<AttachmentUploadResponse> findByTouristPlaceId(Long tourPlaceId);

    void deleteForTouristPlace(Long attachmentId);

    // ---------------- Food ----------------

    AttachmentUploadResponse uploadForFood(Long foodId, MultipartFile file, String type);

    List<AttachmentUploadResponse> uploadMultipleForFood(Long foodId, List<MultipartFile> files, String type);

    List<AttachmentUploadResponse> findByFoodId(Long foodId);

    void deleteForFood(Long foodId, Long attachmentId);

    // ---------------- Restaurant ----------------

    AttachmentUploadResponse uploadForRestaurant(Long restaurantId, MultipartFile file, String type);

    List<AttachmentUploadResponse> uploadMultipleForRestaurant(Long restaurantId, List<MultipartFile> files, String type);

    List<AttachmentUploadResponse> findByRestaurantId(Long restaurantId);

    void deleteForRestaurant(Long restaurantId, Long attachmentId);
}
