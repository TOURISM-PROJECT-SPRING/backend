package com.example.spring_boot_project_api.mapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.example.spring_boot_project_api.dto.request.TourPlaceRequestDTO;
import com.example.spring_boot_project_api.dto.response.TourPlaceResponseDTO;
import com.example.spring_boot_project_api.model.Location;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.Users;

public class TourPlaceMapper {

    public static TourPlaces toEntity(TourPlaceRequestDTO request, PlaceCategoties category, Users user, Location district) {
        TourPlaces place = new TourPlaces();
        place.setName(request.getName());
        place.setDescription(request.getDescription());
        place.setAddress(request.getAddress());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setStaus(request.getStatus());
        place.setRating(request.getRating() != null ? request.getRating() : BigDecimal.ZERO);
        place.setPlaceCategoty(category);
        place.setUser(user);
        place.setDistrict(district);
        return place;
    }

    public static void toEntity(TourPlaces place, TourPlaceRequestDTO request, PlaceCategoties category, Users user, Location district) {
        place.setName(request.getName());
        place.setDescription(request.getDescription());
        place.setAddress(request.getAddress());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setStaus(request.getStatus());
        place.setRating(request.getRating() != null ? request.getRating() : BigDecimal.ZERO);
        place.setPlaceCategoty(category);
        place.setUser(user);
        place.setDistrict(district);
    }

    public static TourPlaceResponseDTO toResponse(TourPlaces place) {
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
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }

    public static List<TourPlaceResponseDTO> toResponseList(List<TourPlaces> places) {
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
                // .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private static TourPlaceResponseDTO.DistrictInfo toDistrictInfo(Location district) {
        if (district == null) return null;
        return TourPlaceResponseDTO.DistrictInfo.builder()
                .id(district.getId())
                .name(district.getDistrict())
                .province(toProvinceInfo(district))
                .build();
    }

    private static TourPlaceResponseDTO.ProvinceInfo toProvinceInfo(Location district) {
        if (district == null) return null;
        return TourPlaceResponseDTO.ProvinceInfo.builder()
                .id(district.getId())
                .name(district.getProvince())
                .build();
    }
}