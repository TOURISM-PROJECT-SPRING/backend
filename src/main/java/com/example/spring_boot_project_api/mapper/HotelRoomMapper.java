package com.example.spring_boot_project_api.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.HotelRoomRequest;
import com.example.spring_boot_project_api.dto.response.HotelRoomResponse;
import com.example.spring_boot_project_api.model.HotelRooms;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.RoomTypes;

public class HotelRoomMapper {

    private HotelRoomMapper() {}

    public static HotelRooms toEntity(HotelRoomRequest request, Hotels hotel, RoomTypes roomType) {
        HotelRooms hotelRoom = new HotelRooms();
        hotelRoom.setHotels(hotel);
        hotelRoom.setRoomTypes(roomType);
        hotelRoom.setTotalRoom(request.getTotalRoom());
        hotelRoom.setCapacity(request.getCapacity());
        hotelRoom.setPricePerNight(request.getPricePerNight());
        return hotelRoom;
    }

    public static void toEntity(HotelRooms hotelRoom, HotelRoomRequest request, Hotels hotel, RoomTypes roomType) {
        hotelRoom.setHotels(hotel);
        hotelRoom.setRoomTypes(roomType);
        hotelRoom.setTotalRoom(request.getTotalRoom());
        hotelRoom.setCapacity(request.getCapacity());
        hotelRoom.setPricePerNight(request.getPricePerNight());
    }

    public static HotelRoomResponse toResponse(HotelRooms hotelRoom) {
        if (hotelRoom == null) return null;
        return HotelRoomResponse.builder()
                .id(hotelRoom.getId())
                .hotelId(hotelRoom.getHotels() != null ? hotelRoom.getHotels().getId() : null)
                .hotelName(hotelRoom.getHotels() != null ? hotelRoom.getHotels().getHotelName() : null)
                .roomTypeId(hotelRoom.getRoomTypes() != null ? hotelRoom.getRoomTypes().getId() : null)
                .roomType(hotelRoom.getRoomTypes() != null ? hotelRoom.getRoomTypes().getRoomType() : null)
                .totalRoom(hotelRoom.getTotalRoom())
                .capacity(hotelRoom.getCapacity())
                .pricePerNight(hotelRoom.getPricePerNight())
                .createdAt(hotelRoom.getCreatedAt())
                .updatedAt(hotelRoom.getUpdatedAt())
                .build();
    }

    public static List<HotelRoomResponse> toResponseList(List<HotelRooms> hotelRooms) {
        if (hotelRooms == null) return Collections.emptyList();
        return hotelRooms.stream()
                .map(HotelRoomMapper::toResponse)
                .collect(Collectors.toList());
    }
}