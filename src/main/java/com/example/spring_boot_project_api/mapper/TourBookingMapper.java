package com.example.spring_boot_project_api.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.spring_boot_project_api.dto.request.TourBookingRequest;
import com.example.spring_boot_project_api.dto.response.TourBookingResponse;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.Users;

public class TourBookingMapper {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private TourBookingMapper() {}

    public static TourBookings toEntity(TourBookingRequest request, Users user, TourPackages tourPackage) {
        TourBookings booking = new TourBookings();
        booking.setUser(user);
        booking.setTourPackages(tourPackage);
        booking.setNumPeople(request.getNumPeople() != null ? request.getNumPeople() : 1);
        booking.setTourDate(request.getTourDate());
        booking.setTotalPrice(request.getTotalPrice() != null
                ? request.getTotalPrice()
                : calculateTotal(tourPackage.getPrice(), request.getNumPeople()));
        booking.setStatus(STATUS_CONFIRMED);
        return booking;
    }

    public static BigDecimal calculateTotal(BigDecimal pricePerPerson, Integer numPeople) {
        if (pricePerPerson == null || numPeople == null) return BigDecimal.ZERO;
        return pricePerPerson.multiply(BigDecimal.valueOf(numPeople));
    }

    public static TourBookingResponse toResponse(TourBookings booking) {
        if (booking == null) return null;
        TourPackages pkg = booking.getTourPackages();
        return TourBookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .userName(booking.getUser() != null ? booking.getUser().getFullname() : null)
                .tourPackageId(pkg != null ? pkg.getId() : null)
                .tourPackageName(pkg != null ? pkg.getName() : null)
                .numPeople(booking.getNumPeople())
                .tourDate(booking.getTourDate())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .paymentMethod(booking.getPaymentMethod())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public static List<TourBookingResponse> toResponseList(List<TourBookings> bookings) {
        if (bookings == null) return Collections.emptyList();
        return bookings.stream()
                .map(TourBookingMapper::toResponse)
                .collect(Collectors.toList());
    }
}
