package com.example.spring_boot_project_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.spring_boot_project_api.model.Reviews;

@Repository
public interface ReviewRepository extends JpaRepository<Reviews, Long> {

    List<Reviews> findByTourPlace_IdOrderByCreatedAtDesc(Long tourPlaceId);

    List<Reviews> findByHotel_IdOrderByCreatedAtDesc(Long hotelId);

    List<Reviews> findByRestaurant_IdOrderByCreatedAtDesc(Long restaurantId);

    List<Reviews> findByTourPackage_IdOrderByCreatedAtDesc(Long tourPackageId);

    List<Reviews> findByUserId(Long userId);

    List<Reviews> findByRating(Integer rating);

    @Query("select avg(r.rating) from Reviews r where r.tourPlace.id = :tourPlaceId")
    Double findAverageRatingByTourPlaceId(@Param("tourPlaceId") Long tourPlaceId);

    @Query("select avg(r.rating) from Reviews r where r.hotel.id = :hotelId")
    Double findAverageRatingByHotelId(@Param("hotelId") Long hotelId);

    @Query("select avg(r.rating) from Reviews r where r.restaurant.id = :restaurantId")
    Double findAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("select avg(r.rating) from Reviews r where r.tourPackage.id = :tourPackageId")
    Double findAverageRatingByTourPackageId(@Param("tourPackageId") Long tourPackageId);
}
