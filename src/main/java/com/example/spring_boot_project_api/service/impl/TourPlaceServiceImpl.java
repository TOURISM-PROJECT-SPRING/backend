package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_project_api.dto.request.TourPlaceRequestDTO;
import com.example.spring_boot_project_api.dto.response.TourPlaceResponseDTO;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.TourPlaceMapper;
import com.example.spring_boot_project_api.model.Districts;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.model.PlaceImages;
import com.example.spring_boot_project_api.model.TourismPlaces;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.DistrictRepository;
import com.example.spring_boot_project_api.repository.PlaceCategoryRepository;
import com.example.spring_boot_project_api.repository.PlaceImageRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.CloudinaryService;
import com.example.spring_boot_project_api.service.TourPlaceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TourPlaceServiceImpl implements TourPlaceService {

    private final TourismPlaceRepository tourPlaceRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final PlaceImageRepository placeImageRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public TourPlaceResponseDTO create(TourPlaceRequestDTO request, MultipartFile[] images) {
        PlaceCategoties category = placeCategoryRepository.findById(request.getPlaceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Place Category", request.getPlaceCategoryId()));

        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Districts district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", request.getDistrictId()));

        TourismPlaces place = TourPlaceMapper.toEntity(request, category, user, district);
        TourismPlaces saved = tourPlaceRepository.save(place);
        addImagesInternal(saved, images);
        return TourPlaceMapper.toResponse(reload(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public TourPlaceResponseDTO getById(Long id) {
        TourismPlaces place = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));
        return TourPlaceMapper.toResponse(place);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getAll() {
        List<TourismPlaces> places = tourPlaceRepository.findAll();
        return TourPlaceMapper.toResponseList(places);
    }

    @Override
    public TourPlaceResponseDTO update(Long id, TourPlaceRequestDTO request, MultipartFile[] images) {
        TourismPlaces existing = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));

        PlaceCategoties category = placeCategoryRepository.findById(request.getPlaceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Place Category", request.getPlaceCategoryId()));

        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Districts district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", request.getDistrictId()));

        TourPlaceMapper.toEntity(existing, request, category, user, district);
        TourismPlaces updated = tourPlaceRepository.save(existing);
        addImagesInternal(updated, images);
        return TourPlaceMapper.toResponse(reload(updated));
    }

    @Override
    public TourPlaceResponseDTO addImages(Long id, MultipartFile[] images) {
        TourismPlaces place = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));
        addImagesInternal(place, images);
        return TourPlaceMapper.toResponse(reload(place));
    }

    @Override
    public TourPlaceResponseDTO removeImage(Long id, Long imageId) {
        TourismPlaces place = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));

        PlaceImages image = placeImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Place Image", imageId));

        if (!image.getTourismPlace().getId().equals(place.getId())) {
            throw new IllegalArgumentException("Image does not belong to this tourism place");
        }

        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());
        placeImageRepository.delete(image);

        if (wasPrimary) {
            placeImageRepository.findByTourismPlace_Id(id).stream()
                    .findFirst()
                    .ifPresent(this::setPrimary);
        }
        return TourPlaceMapper.toResponse(reload(place));
    }

    @Override
    public TourPlaceResponseDTO clearImages(Long id) {
        TourismPlaces place = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));
        placeImageRepository.deleteByTourismPlace_Id(place.getId());
        return TourPlaceMapper.toResponse(reload(place));
    }

    @Override
    public TourPlaceResponseDTO setPrimaryImage(Long id, Long imageId) {
        TourismPlaces place = tourPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", id));

        PlaceImages image = placeImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Place Image", imageId));

        if (!image.getTourismPlace().getId().equals(place.getId())) {
            throw new IllegalArgumentException("Image does not belong to this tourism place");
        }

        placeImageRepository.findByTourismPlace_IdAndIsPrimaryTrue(place.getId())
                .forEach(img -> {
                    img.setIsPrimary(false);
                    placeImageRepository.save(img);
                });

        setPrimary(image);
        return TourPlaceMapper.toResponse(reload(place));
    }

    @Override
    public void delete(Long id) {
        if (!tourPlaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tourism Place", id);
        }
        tourPlaceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> searchByName(String keyword) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByDistrictId(Long districtId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByDistrictId(districtId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByCategoryId(Long categoryId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByPlaceCategotyId(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByUserId(Long userId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByStatus(String status) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByStaus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByMinRating(BigDecimal minRating) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByRatingGreaterThanEqualOrderByRatingDesc(minRating));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourPlaceResponseDTO> getByDistrictAndCategory(Long districtId, Long categoryId) {
        return TourPlaceMapper.toResponseList(tourPlaceRepository.findByDistrictIdAndPlaceCategotyId(districtId, categoryId));
    }

    private void addImagesInternal(TourismPlaces place, MultipartFile[] images) {
        if (images == null) return;

        List<PlaceImages> existing = placeImageRepository.findByTourismPlace_Id(place.getId());
        boolean hasPrimary = existing.stream().anyMatch(img -> Boolean.TRUE.equals(img.getIsPrimary()));

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) continue;
            String imageUrl;
            try {
                imageUrl = cloudinaryService.uploadImage(image);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
            PlaceImages placeImage = new PlaceImages();
            placeImage.setImageUrl(imageUrl);
            placeImage.setIsPrimary(!hasPrimary);
            placeImage.setTourismPlace(place);
            placeImageRepository.save(placeImage);
            hasPrimary = true;
        }
    }

    private void setPrimary(PlaceImages image) {
        image.setIsPrimary(true);
        placeImageRepository.save(image);
    }

    private TourismPlaces reload(TourismPlaces place) {
        return tourPlaceRepository.findById(place.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tourism Place", place.getId()));
    }
}
