package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.request.RestaurantRequest;
import com.example.spring_boot_project_api.dto.response.RestaurantResponse;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.service.impl.RestaurantServiceImpl;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private TourismPlaceRepository tourismPlaceRepository;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private TourPlaces tourismPlace() {
        TourPlaces place = new TourPlaces();
        place.setId(1L);
        place.setName("Angkor Wat");
        return place;
    }

    private RestaurantRequest request() {
        RestaurantRequest request = new RestaurantRequest();
        request.setName("Khmer Kitchen");
        request.setDescription("Traditional Khmer food");
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(22, 0));
        request.setTourismPlaceId(1L);
        return request;
    }

    private Restaurants restaurant() {
        Restaurants restaurant = new Restaurants();
        restaurant.setId(5L);
        restaurant.setName("Khmer Kitchen");
        restaurant.setDescription("Traditional Khmer food");
        restaurant.setOpenTime(LocalTime.of(9, 0));
        restaurant.setClossTime(LocalTime.of(22, 0));
        restaurant.setTourPlaces(tourismPlace());
        return restaurant;
    }

    @Test
    void create_mapsRequestAndSaves() {
        when(tourismPlaceRepository.findById(1L)).thenReturn(Optional.of(tourismPlace()));
        when(restaurantRepository.save(any(Restaurants.class))).thenAnswer(inv -> {
            Restaurants r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });

        RestaurantResponse response = restaurantService.create(request());

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("Khmer Kitchen");
        assertThat(response.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(response.getCloseTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(response.getTourismPlaceId()).isEqualTo(1L);
        assertThat(response.getTourismPlaceName()).isEqualTo("Angkor Wat");
    }

    @Test
    void create_rejectsCloseTimeBeforeOpenTime() {
        RestaurantRequest bad = request();
        bad.setOpenTime(LocalTime.of(22, 0));
        bad.setCloseTime(LocalTime.of(9, 0));

        assertThatThrownBy(() -> restaurantService.create(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Close time");

        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void findByTourismPlaceId_returnsRestaurants() {
        when(tourismPlaceRepository.existsById(1L)).thenReturn(true);
        when(restaurantRepository.findByTourPlacesId(1L)).thenReturn(List.of(restaurant()));

        List<RestaurantResponse> responses = restaurantService.findByTourismPlaceId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Khmer Kitchen");
    }

    @Test
    void findByTourismPlaceId_throwsWhenPlaceMissing() {
        when(tourismPlaceRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.findByTourismPlaceId(1L))
                .isInstanceOf(RuntimeException.class);
    }
}
