package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.RoomRequest;
import com.example.spring_boot_project_api.dto.response.RoomResponse;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.RoomTypes;

public class RoomMapper {

    private RoomMapper() {}

    public static Rooms toEntity(RoomRequest request, Hotels hotel, RoomTypes roomType) {
        Rooms room = new Rooms();
        room.setHotels(hotel);
        room.setRoomTypes(roomType);
        return room;
    }

    public static void toEntity(Rooms room, RoomRequest request, Hotels hotel, RoomTypes roomType) {
        room.setHotels(hotel);
        room.setRoomTypes(roomType);
    }

    public static RoomResponse toResponse(Rooms room) {
        if (room == null) return null;
        return RoomResponse.builder()
                .id(room.getId())
                .hotelId(room.getHotels() != null ? room.getHotels().getId() : null)
                .hotelName(room.getHotels() != null ? room.getHotels().getHotelName() : null)
                .roomTypeId(room.getRoomTypes() != null ? room.getRoomTypes().getId() : null)
                .roomType(room.getRoomTypes() != null ? room.getRoomTypes().getRoomType() : null)
                .capacity(room.getRoomTypes() != null ? room.getRoomTypes().getCapacity() : null)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    public static List<RoomResponse> toResponseList(List<Rooms> rooms) {
        if (rooms == null) return Collections.emptyList();
        return rooms.stream()
                .map(RoomMapper::toResponse)
                .collect(Collectors.toList());
    }
}