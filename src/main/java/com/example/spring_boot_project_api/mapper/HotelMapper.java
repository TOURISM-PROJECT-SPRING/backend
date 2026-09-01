package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.HotelRequest;
import com.example.spring_boot_project_api.dto.response.HotelResponse;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Location;
import com.example.spring_boot_project_api.model.Users;

public class HotelMapper {

    private HotelMapper() {}

    public static Hotels toEntity(HotelRequest request, Location location, Users owner) {
        Hotels hotel = new Hotels();
        hotel.setHotelName(request.getHotelName());
        hotel.setPhoneContact(request.getPhoneContact());
        hotel.setEmailContact(request.getEmailContact());
        hotel.setLocation(location);
        hotel.setOwner(owner);
        return hotel;
    }

    public static void toEntity(Hotels hotel, HotelRequest request, Location location, Users owner) {
        hotel.setHotelName(request.getHotelName());
        hotel.setPhoneContact(request.getPhoneContact());
        hotel.setEmailContact(request.getEmailContact());
        hotel.setLocation(location);
        hotel.setOwner(owner);
    }

    public static HotelResponse toResponse(Hotels hotel) {
        if (hotel == null) return null;
        return HotelResponse.builder()
                .id(hotel.getId())
                .hotelName(hotel.getHotelName())
                .phoneContact(hotel.getPhoneContact())
                .emailContact(hotel.getEmailContact())
                .locationId(hotel.getLocation() != null ? hotel.getLocation().getId() : null)
                .locationName(hotel.getLocation() != null ? hotel.getLocation().getDistrict() : null)
                .ownerId(hotel.getOwner() != null ? hotel.getOwner().getId() : null)
                .ownerName(hotel.getOwner() != null ? hotel.getOwner().getFullname() : null)
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }

    public static List<HotelResponse> toResponseList(List<Hotels> hotels) {
        if (hotels == null) return Collections.emptyList();
        return hotels.stream()
                .map(HotelMapper::toResponse)
                .collect(Collectors.toList());
    }
}