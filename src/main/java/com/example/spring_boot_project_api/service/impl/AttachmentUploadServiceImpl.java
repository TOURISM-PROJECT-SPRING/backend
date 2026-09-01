package com.example.spring_boot_project_api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.response.AttachmentUploadResponse;
import com.example.spring_boot_project_api.enums.AttachmentFileType;
import com.example.spring_boot_project_api.enums.AttachmentRole;
import com.example.spring_boot_project_api.exception.InvalidFileException;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.model.Attachments;
import com.example.spring_boot_project_api.model.FoodAttachments;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.HotelAttachments;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.RoomAttachments;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.RestaurantAttachments;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.TourPlaceAttachments;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.UserAttachments;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.AttachmentRepository;
import com.example.spring_boot_project_api.repository.FoodAttachmentRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.HotelAttachmentRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.RestaurantAttachmentRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.RoomAttachmentRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.TourPlaceAttachmentRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserAttachmentRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.AttachmentUploadService;
import com.example.spring_boot_project_api.service.CloudinaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentUploadServiceImpl implements AttachmentUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Map<String, AttachmentFileType> ALLOWED_MIME_TYPES = Map.ofEntries(
            Map.entry("image/jpeg", AttachmentFileType.IMAGE),
            Map.entry("image/png", AttachmentFileType.IMAGE),
            Map.entry("image/webp", AttachmentFileType.IMAGE),
            Map.entry("application/pdf", AttachmentFileType.DOCUMENT),
            Map.entry("application/msword", AttachmentFileType.DOCUMENT),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    AttachmentFileType.DOCUMENT)
    );

    private final CloudinaryService cloudinaryService;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final UserAttachmentRepository userAttachmentRepository;
    private final HotelRepository hotelRepository;
    private final HotelAttachmentRepository hotelAttachmentRepository;
    private final RoomRepository roomRepository;
    private final RoomAttachmentRepository roomAttachmentRepository;
    private final TourismPlaceRepository tourismPlaceRepository;
    private final TourPlaceAttachmentRepository tourPlaceAttachmentRepository;
    private final FoodRepository foodRepository;
    private final FoodAttachmentRepository foodAttachmentRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantAttachmentRepository restaurantAttachmentRepository;

    // ------------------------------------------------------------------
    // User
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AttachmentUploadResponse uploadForUser(Long userId, MultipartFile file, String type) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        validateRole(type);
        return doUpload(file, type, "users/" + userId, junction -> {
            UserAttachments j = new UserAttachments();
            j.setUsers(user);
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return userAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional
    public List<AttachmentUploadResponse> uploadMultipleForUser(Long userId,
                                                                List<MultipartFile> files, String type) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        validateRole(type);
        return doUploadMultiple(files, type, "users/" + userId, junction -> {
            UserAttachments j = new UserAttachments();
            j.setUsers(userRepository.getReferenceById(userId));
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return userAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentUploadResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return mapJunctions(userAttachmentRepository.findByUsers_Id(userId),
                j -> new MappedJunction(j.getAttachments(), j.getType(), j.getSortOrder()));
    }

    @Override
    @Transactional
    public void deleteForUser(Long userId, Long attachmentId) {
        UserAttachments junction = userAttachmentRepository
                .findByUsers_IdAndAttachments_Id(userId, attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User Attachment for user " + userId + " and attachment " + attachmentId));
        userAttachmentRepository.delete(junction);
        deleteAttachmentIfOrphaned(attachmentId);
    }

    // ------------------------------------------------------------------
    // Hotel
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AttachmentUploadResponse uploadForHotel(Long hotelId, MultipartFile file, String type) {
        Hotels hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        validateRole(type);
        return doUpload(file, type, "hotels/" + hotelId, junction -> {
            HotelAttachments j = new HotelAttachments();
            j.setHotels(hotel);
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return hotelAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional
    public List<AttachmentUploadResponse> uploadMultipleForHotel(Long hotelId,
                                                                 List<MultipartFile> files, String type) {
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        validateRole(type);
        return doUploadMultiple(files, type, "hotels/" + hotelId, junction -> {
            HotelAttachments j = new HotelAttachments();
            j.setHotels(hotelRepository.getReferenceById(hotelId));
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return hotelAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentUploadResponse> findByHotelId(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
        return mapJunctions(hotelAttachmentRepository.findByHotels_Id(hotelId),
                j -> new MappedJunction(j.getAttachments(), j.getType(), j.getSortOrder()));
    }

    @Override
    @Transactional
    public void deleteForHotel(Long hotelId, Long attachmentId) {
        HotelAttachments junction = hotelAttachmentRepository
                .findByHotels_IdAndAttachments_Id(hotelId, attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel Attachment for hotel " + hotelId + " and attachment " + attachmentId));
        hotelAttachmentRepository.delete(junction);
        deleteAttachmentIfOrphaned(attachmentId);
    }

    // ------------------------------------------------------------------
    // Room
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AttachmentUploadResponse uploadForRoom(Long roomId, MultipartFile file, String type) {
        Rooms room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        validateRole(type);
        return doUpload(file, type, "rooms/" + roomId, junction -> {
            RoomAttachments j = new RoomAttachments();
            j.setRooms(room);
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return roomAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional
    public List<AttachmentUploadResponse> uploadMultipleForRoom(Long roomId,
                                                                List<MultipartFile> files, String type) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        validateRole(type);
        return doUploadMultiple(files, type, "rooms/" + roomId, junction -> {
            RoomAttachments j = new RoomAttachments();
            j.setRooms(roomRepository.getReferenceById(roomId));
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return roomAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentUploadResponse> findByRoomId(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room", roomId);
        }
        return mapJunctions(roomAttachmentRepository.findByRooms_Id(roomId),
                j -> new MappedJunction(j.getAttachments(), j.getType(), j.getSortOrder()));
    }

    @Override
    @Transactional
    public void deleteForRoom(Long roomId, Long attachmentId) {
        RoomAttachments junction = roomAttachmentRepository
                .findByRooms_IdAndAttachments_Id(roomId, attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room Attachment for room " + roomId + " and attachment " + attachmentId));
        roomAttachmentRepository.delete(junction);
        deleteAttachmentIfOrphaned(attachmentId);
    }

    // ------------------------------------------------------------------
    // Tour place
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AttachmentUploadResponse uploadForTouristPlace(Long tourPlaceId, MultipartFile file, String type) {
        TourPlaces place = tourismPlaceRepository.findById(tourPlaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour Place", tourPlaceId));
        validateRole(type);
        return doUpload(file, type, "tour-places/" + tourPlaceId, junction -> {
            TourPlaceAttachments j = new TourPlaceAttachments();
            j.setTourPlaces(place);
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return tourPlaceAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional
    public List<AttachmentUploadResponse> uploadMultipleForTouristPlace(Long tourPlaceId,
                                                                        List<MultipartFile> files, String type) {
        tourismPlaceRepository.findById(tourPlaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour Place", tourPlaceId));
        validateRole(type);
        return doUploadMultiple(files, type, "tour-places/" + tourPlaceId, junction -> {
            TourPlaceAttachments j = new TourPlaceAttachments();
            j.setTourPlaces(tourismPlaceRepository.getReferenceById(tourPlaceId));
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return tourPlaceAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentUploadResponse> findByTouristPlaceId(Long tourPlaceId) {
        if (!tourismPlaceRepository.existsById(tourPlaceId)) {
            throw new ResourceNotFoundException("Tour Place", tourPlaceId);
        }
        return mapJunctions(tourPlaceAttachmentRepository.findByTourPlaces_Id(tourPlaceId),
                j -> new MappedJunction(j.getAttachments(), j.getType(), j.getSortOrder()));
    }

    @Override
    @Transactional
    public void deleteForTouristPlace(Long attachmentId) {
        List<TourPlaceAttachments> junctions = tourPlaceAttachmentRepository.findByAttachments_Id(attachmentId);
        if (junctions.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Tour Place Attachment for attachment " + attachmentId);
        }
        tourPlaceAttachmentRepository.deleteAll(junctions);
        deleteAttachmentIfOrphaned(attachmentId);
    }

    // ------------------------------------------------------------------
    // Food
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AttachmentUploadResponse uploadForFood(Long foodId, MultipartFile file, String type) {
        Foods food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food", foodId));
        validateRole(type);
        return doUpload(file, type, "foods/" + foodId, junction -> {
            FoodAttachments j = new FoodAttachments();
            j.setFoods(food);
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return foodAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional
    public List<AttachmentUploadResponse> uploadMultipleForFood(Long foodId,
                                                                List<MultipartFile> files, String type) {
        foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food", foodId));
        validateRole(type);
        return doUploadMultiple(files, type, "foods/" + foodId, junction -> {
            FoodAttachments j = new FoodAttachments();
            j.setFoods(foodRepository.getReferenceById(foodId));
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return foodAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentUploadResponse> findByFoodId(Long foodId) {
        if (!foodRepository.existsById(foodId)) {
            throw new ResourceNotFoundException("Food", foodId);
        }
        return mapJunctions(foodAttachmentRepository.findByFoods_Id(foodId),
                j -> new MappedJunction(j.getAttachments(), j.getType(), j.getSortOrder()));
    }

    @Override
    @Transactional
    public void deleteForFood(Long foodId, Long attachmentId) {
        FoodAttachments junction = foodAttachmentRepository
                .findByFoods_IdAndAttachments_Id(foodId, attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food Attachment for food " + foodId + " and attachment " + attachmentId));
        foodAttachmentRepository.delete(junction);
        deleteAttachmentIfOrphaned(attachmentId);
    }

    // ------------------------------------------------------------------
    // Restaurant
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AttachmentUploadResponse uploadForRestaurant(Long restaurantId, MultipartFile file, String type) {
        Restaurants restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));
        validateRole(type);
        return doUpload(file, type, "restaurants/" + restaurantId, junction -> {
            RestaurantAttachments j = new RestaurantAttachments();
            j.setRestaurants(restaurant);
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return restaurantAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional
    public List<AttachmentUploadResponse> uploadMultipleForRestaurant(Long restaurantId,
                                                                      List<MultipartFile> files, String type) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));
        validateRole(type);
        return doUploadMultiple(files, type, "restaurants/" + restaurantId, junction -> {
            RestaurantAttachments j = new RestaurantAttachments();
            j.setRestaurants(restaurantRepository.getReferenceById(restaurantId));
            j.setAttachments(junction.attachment);
            j.setType(junction.type);
            j.setSortOrder(junction.sortOrder);
            return restaurantAttachmentRepository.save(j);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentUploadResponse> findByRestaurantId(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }
        return mapJunctions(restaurantAttachmentRepository.findByRestaurants_Id(restaurantId),
                j -> new MappedJunction(j.getAttachments(), j.getType(), j.getSortOrder()));
    }

    @Override
    @Transactional
    public void deleteForRestaurant(Long restaurantId, Long attachmentId) {
        RestaurantAttachments junction = restaurantAttachmentRepository
                .findByRestaurants_IdAndAttachments_Id(restaurantId, attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant Attachment for restaurant " + restaurantId + " and attachment " + attachmentId));
        restaurantAttachmentRepository.delete(junction);
        deleteAttachmentIfOrphaned(attachmentId);
    }

    // ------------------------------------------------------------------
    // Shared delete (reference-counting across the 6 junction tables)
    // ------------------------------------------------------------------

    private void deleteAttachmentIfOrphaned(Long attachmentId) {
        if (isReferenced(attachmentId)) {
            return;
        }
        Attachments attachment = attachmentRepository.findById(attachmentId)
                .orElse(null);
        if (attachment == null) {
            return;
        }
        String publicId = attachment.getCloudinaryPublicId();
        String resourceType = attachment.getCloudinaryResourceType();
        attachmentRepository.delete(attachment);
        if (publicId != null) {
            cloudinaryService.delete(publicId, resourceType);
        }
    }

    private boolean isReferenced(Long attachmentId) {
        return userAttachmentRepository.existsByAttachments_Id(attachmentId)
                || hotelAttachmentRepository.existsByAttachments_Id(attachmentId)
                || roomAttachmentRepository.existsByAttachments_Id(attachmentId)
                || tourPlaceAttachmentRepository.existsByAttachments_Id(attachmentId)
                || foodAttachmentRepository.existsByAttachments_Id(attachmentId)
                || restaurantAttachmentRepository.existsByAttachments_Id(attachmentId);
    }

    // ------------------------------------------------------------------
    // Shared upload plumbing
    // ------------------------------------------------------------------

    private void validateRole(String type) {
        if (type != null && !type.isBlank() && !AttachmentRole.isValid(type)) {
            throw new InvalidFileException("Invalid attachment role: " + type);
        }
    }

    private AttachmentUploadResponse doUpload(MultipartFile file, String type, String baseFolder,
                                              JunctionSaver saver) {
        List<MultipartFile> files = new ArrayList<>();
        files.add(file);
        List<AttachmentUploadResponse> responses = doUploadMultiple(files, type, baseFolder, saver);
        return responses.get(0);
    }

    private List<AttachmentUploadResponse> doUploadMultiple(List<MultipartFile> files, String type,
                                                            String baseFolder, JunctionSaver saver) {
        validateFiles(files);

        List<StoredJunction> stored = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String mime = file.getContentType().toLowerCase(Locale.ROOT);
                AttachmentFileType fileType = ALLOWED_MIME_TYPES.get(mime);

                CloudinaryService.UploadResult result =
                        cloudinaryService.uploadImage(file, baseFolder, fileType);

                Attachments attachment = new Attachments();
                attachment.setFileName(publicIdTail(result.publicId()) + extensionFor(mime));
                attachment.setOriginalName(StringUtils.hasText(file.getOriginalFilename())
                        ? file.getOriginalFilename() : result.publicId());
                attachment.setCloudinaryUrl(result.secureUrl());
                attachment.setCloudinaryPublicId(result.publicId());
                attachment.setCloudinaryResourceType(result.resourceType());
                attachment.setFileType(fileType);
                attachment.setMimeType(mime);
                attachment.setFileSize(file.getSize());

                Attachments saved = attachmentRepository.save(attachment);

                int sortOrder = stored.size();
                saver.save(new JuncData(saved, type, sortOrder));

                stored.add(new StoredJunction(saved, result.publicId(), type, sortOrder));
            }
        } catch (RuntimeException ex) {
            // Roll back Cloudinary uploads if the DB transaction failed part-way:
            // do not leave orphaned Cloudinary images behind.
            for (StoredJunction s : stored) {
                try {
                    cloudinaryService.delete(s.publicId());
                } catch (RuntimeException ignored) {
                    // best-effort cleanup
                }
            }
            throw ex;
        }

        List<AttachmentUploadResponse> responses = new ArrayList<>();
        for (StoredJunction s : stored) {
            responses.add(toResponse(s.attachment, s.type, s.sortOrder));
        }
        return responses;
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null) {
            throw new InvalidFileException("No files were provided for upload");
        }
        for (MultipartFile file : files) {
            validateFile(file);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Cannot upload an empty file");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException(
                    "File \"" + file.getOriginalFilename() + "\" exceeds the 10MB size limit");
        }
        String mime = file.getContentType();
        if (mime == null || !ALLOWED_MIME_TYPES.containsKey(mime.toLowerCase(Locale.ROOT))) {
            throw new InvalidFileException(
                    "Unsupported file type: " + (mime == null ? "unknown" : mime));
        }
    }

    private String extensionFor(String mime) {
        return switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            case "application/msword" -> ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            default -> "";
        };
    }

    private String publicIdTail(String publicId) {
        int idx = publicId.lastIndexOf('/');
        return idx >= 0 ? publicId.substring(idx + 1) : publicId;
    }

    private <T> List<AttachmentUploadResponse> mapJunctions(List<T> junctions,
                                                            Function<T, MappedJunction> reader) {
        List<AttachmentUploadResponse> responses = new ArrayList<>();
        for (T j : junctions) {
            MappedJunction m = reader.apply(j);
            responses.add(toResponse(m.attachment(), m.type(), m.sortOrder()));
        }
        return responses;
    }

    private AttachmentUploadResponse toResponse(Attachments a, String type, Integer sortOrder) {
        return AttachmentUploadResponse.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .originalName(a.getOriginalName())
                .fileType(a.getFileType())
                .mimeType(a.getMimeType())
                .fileSize(a.getFileSize())
                .cloudinaryUrl(a.getCloudinaryUrl())
                .type(type)
                .sortOrder(sortOrder)
                .createdAt(a.getCreatedAt())
                .build();
    }

    private record StoredJunction(Attachments attachment, String publicId,
                                  String type, Integer sortOrder) {}

    private record JuncData(Attachments attachment, String type, Integer sortOrder) {}

    private record MappedJunction(Attachments attachment, String type, Integer sortOrder) {}

    @FunctionalInterface
    private interface JunctionSaver {
        Object save(JuncData data);
    }
}