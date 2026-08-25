package com.example.spring_boot_project_api.mapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.example.spring_boot_project_api.dto.request.TourPlaceRequestDTO;
import com.example.spring_boot_project_api.dto.response.TourPlaceResponseDTO;
import com.example.spring_boot_project_api.model.Districts;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.model.PlaceImages;
import com.example.spring_boot_project_api.model.Provinces;
import com.example.spring_boot_project_api.model.TourismPlaces;
import com.example.spring_boot_project_api.model.Users;

public class TourPlaceMapper {

    public static TourismPlaces toEntity(TourPlaceRequestDTO request, PlaceCategoties category, Users user, Districts district) {
        TourismPlaces place = new TourismPlaces();
        place.setName(request.getName());
        place.setDescription(request.getDescription());
        place.setAddress(request.getAddress());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setStaus(request.getStatus());
        place.setRating(request.getRating());
        place.setPlaceCategoty(category);
        place.setUser(user);
        place.setDistrict(district);
        return place;
    }

    public static void toEntity(TourismPlaces place, TourPlaceRequestDTO request, PlaceCategoties category, Users user, Districts district) {
        place.setName(request.getName());
        place.setDescription(request.getDescription());
        place.setAddress(request.getAddress());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setStaus(request.getStatus());
        place.setRating(request.getRating());
        place.setPlaceCategoty(category);
        place.setUser(user);
        place.setDistrict(district);
    }

    public static TourPlaceResponseDTO toResponse(TourismPlaces place) {
        if (place == null) return null;

        return TourPlaceResponseDTO.builder()
                .id(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .status(place.getStaus())
                .rating(place.getRating())
                .placeCategory(toCategoryInfo(place.getPlaceCategoty()))
                .user(toUserInfo(place.getUser()))
                .district(toDistrictInfo(place.getDistrict()))
                .placeImages(toImageInfoList(place.getPlaceImages()))
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }

    public static List<TourPlaceResponseDTO> toResponseList(List<TourismPlaces> places) {
        if (places == null) return Collections.emptyList();
        return places.stream()
                .map(TourPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }

    private static TourPlaceResponseDTO.PlaceCategoryInfo toCategoryInfo(PlaceCategoties category) {
        if (category == null) return null;
        return TourPlaceResponseDTO.PlaceCategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .image(category.getImage())
                .build();
    }

    private static TourPlaceResponseDTO.UserInfo toUserInfo(Users user) {
        if (user == null) return null;
        return TourPlaceResponseDTO.UserInfo.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private static TourPlaceResponseDTO.DistrictInfo toDistrictInfo(Districts district) {
        if (district == null) return null;
        return TourPlaceResponseDTO.DistrictInfo.builder()
                .id(district.getId())
                .name(district.getName())
                .province(toProvinceInfo(district.getProvinces()))
                .build();
    }

    private static TourPlaceResponseDTO.ProvinceInfo toProvinceInfo(Provinces province) {
        if (province == null) return null;
        return TourPlaceResponseDTO.ProvinceInfo.builder()
                .id(province.getId())
                .name(province.getName())
                .build();
    }

    private static List<TourPlaceResponseDTO.PlaceImageInfo> toImageInfoList(List<PlaceImages> images) {
        if (images == null) return new ArrayList<>();
        return images.stream()
                .map(img -> TourPlaceResponseDTO.PlaceImageInfo.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
    }
}