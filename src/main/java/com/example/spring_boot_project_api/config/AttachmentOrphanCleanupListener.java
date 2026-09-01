package com.example.spring_boot_project_api.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.AttachmentRepository;
import com.example.spring_boot_project_api.repository.FoodAttachmentRepository;
import com.example.spring_boot_project_api.repository.HotelAttachmentRepository;
import com.example.spring_boot_project_api.repository.RestaurantAttachmentRepository;
import com.example.spring_boot_project_api.repository.RoomAttachmentRepository;
import com.example.spring_boot_project_api.repository.TourPlaceAttachmentRepository;
import com.example.spring_boot_project_api.repository.UserAttachmentRepository;
import com.example.spring_boot_project_api.service.CloudinaryService;

import jakarta.persistence.PreRemove;

/**
 * Cleans up attachments that become orphans when one of the six owning
 * entities (User/Hotel/Room/TourPlace/Food/Restaurant) is deleted.
 *
 * <p>The junction rows are removed by Hibernate cascade (with orphanRemoval),
 * but the {@code attachments} rows and the Cloudinary images are independent
 * and would otherwise be left behind. This listener runs before the owning
 * entity is removed and, for each attachment used only by that entity, deletes
 * the Attachment record and its Cloudinary image (by {@code public_id}).</p>
 *
 * <p>The repositories are injected lazily: Hibernate instantiates this bean
 * while building the {@code entityManagerFactory}, so eager resolution of the
 * JPA repositories would create a circular dependency with the
 * SessionFactory.</p>
 */
@Component
public class AttachmentOrphanCleanupListener {

    private final UserAttachmentRepository userAttachmentRepository;
    private final HotelAttachmentRepository hotelAttachmentRepository;
    private final RoomAttachmentRepository roomAttachmentRepository;
    private final TourPlaceAttachmentRepository tourPlaceAttachmentRepository;
    private final FoodAttachmentRepository foodAttachmentRepository;
    private final RestaurantAttachmentRepository restaurantAttachmentRepository;
    private final AttachmentRepository attachmentRepository;
    private final CloudinaryService cloudinaryService;

    public AttachmentOrphanCleanupListener(
            @Lazy UserAttachmentRepository userAttachmentRepository,
            @Lazy HotelAttachmentRepository hotelAttachmentRepository,
            @Lazy RoomAttachmentRepository roomAttachmentRepository,
            @Lazy TourPlaceAttachmentRepository tourPlaceAttachmentRepository,
            @Lazy FoodAttachmentRepository foodAttachmentRepository,
            @Lazy RestaurantAttachmentRepository restaurantAttachmentRepository,
            @Lazy AttachmentRepository attachmentRepository,
            @Lazy CloudinaryService cloudinaryService) {
        this.userAttachmentRepository = userAttachmentRepository;
        this.hotelAttachmentRepository = hotelAttachmentRepository;
        this.roomAttachmentRepository = roomAttachmentRepository;
        this.tourPlaceAttachmentRepository = tourPlaceAttachmentRepository;
        this.foodAttachmentRepository = foodAttachmentRepository;
        this.restaurantAttachmentRepository = restaurantAttachmentRepository;
        this.attachmentRepository = attachmentRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @PreRemove
    public void preRemove(Object entity) {
        List<Long> attachmentIds = attachmentIdsFor(entity);
        if (attachmentIds.isEmpty()) {
            return;
        }
        List<Long> orphans = new ArrayList<>();
        for (Long attachmentId : attachmentIds) {
            if (!referencedElsewhere(entity, attachmentId)) {
                orphans.add(attachmentId);
            }
        }
        if (orphans.isEmpty()) {
            return;
        }
        scheduleCleanup(orphans);
    }

    private List<Long> attachmentIdsFor(Object entity) {
        List<Long> ids = new ArrayList<>();
        if (entity instanceof Users u) {
            userAttachmentRepository.findByUsers_Id(u.getId())
                    .forEach(j -> ids.add(j.getAttachments().getId()));
        } else if (entity instanceof Hotels h) {
            hotelAttachmentRepository.findByHotels_Id(h.getId())
                    .forEach(j -> ids.add(j.getAttachments().getId()));
        } else if (entity instanceof Rooms r) {
            roomAttachmentRepository.findByRooms_Id(r.getId())
                    .forEach(j -> ids.add(j.getAttachments().getId()));
        } else if (entity instanceof TourPlaces tp) {
            tourPlaceAttachmentRepository.findByTourPlaces_Id(tp.getId())
                    .forEach(j -> ids.add(j.getAttachments().getId()));
        } else if (entity instanceof Foods f) {
            foodAttachmentRepository.findByFoods_Id(f.getId())
                    .forEach(j -> ids.add(j.getAttachments().getId()));
        } else if (entity instanceof Restaurants r) {
            restaurantAttachmentRepository.findByRestaurants_Id(r.getId())
                    .forEach(j -> ids.add(j.getAttachments().getId()));
        }
        return ids;
    }

    private boolean referencedElsewhere(Object entity, Long attachmentId) {
        // The current owner's junctions are all going away with this deletion,
        // so an attachment is orphaned unless referenced by one of the OTHER
        // four junction tables.
        boolean userRef = !(entity instanceof Users) && userAttachmentRepository.existsByAttachments_Id(attachmentId);
        boolean hotelRef = !(entity instanceof Hotels) && hotelAttachmentRepository.existsByAttachments_Id(attachmentId);
        boolean roomRef = !(entity instanceof Rooms) && roomAttachmentRepository.existsByAttachments_Id(attachmentId);
        boolean tourRef = !(entity instanceof TourPlaces) && tourPlaceAttachmentRepository.existsByAttachments_Id(attachmentId);
        boolean foodRef = !(entity instanceof Foods) && foodAttachmentRepository.existsByAttachments_Id(attachmentId);
        boolean restaurantRef = !(entity instanceof Restaurants) && restaurantAttachmentRepository.existsByAttachments_Id(attachmentId);
        return userRef || hotelRef || roomRef || tourRef || foodRef || restaurantRef;
    }

    private void scheduleCleanup(List<Long> attachmentIds) {
        Runnable cleanup = () -> {
            for (Long attachmentId : attachmentIds) {
                attachmentRepository.findById(attachmentId).ifPresent(attachment -> {
                    String publicId = attachment.getCloudinaryPublicId();
                    String resourceType = attachment.getCloudinaryResourceType();
                    attachmentRepository.delete(attachment);
                    if (publicId != null) {
                        cloudinaryService.delete(publicId, resourceType);
                    }
                });
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cleanup.run();
                }
            });
        } else {
            cleanup.run();
        }
    }
}