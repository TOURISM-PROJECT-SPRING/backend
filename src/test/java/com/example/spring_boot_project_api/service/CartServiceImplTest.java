package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.request.CartItemRequest;
import com.example.spring_boot_project_api.dto.response.CartItemResponse;
import com.example.spring_boot_project_api.dto.response.CartResponse;
import com.example.spring_boot_project_api.model.CartItems;
import com.example.spring_boot_project_api.model.Carts;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.CartItemRepository;
import com.example.spring_boot_project_api.repository.CartRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.impl.CartServiceImpl;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Users user() {
        Users user = new Users();
        user.setId(1L);
        user.setFullname("Piseth");
        return user;
    }

    private Restaurants restaurant() {
        Restaurants restaurant = new Restaurants();
        restaurant.setId(2L);
        restaurant.setName("Khmer Kitchen");
        return restaurant;
    }

    private Foods food() {
        Foods food = new Foods();
        food.setId(3L);
        food.setName("Amok");
        food.setPrice(new BigDecimal("10.00"));
        food.setIsAvailable(true);
        food.setRestaurants(restaurant());
        return food;
    }

    private Carts cart() {
        Carts cart = new Carts();
        cart.setId(9L);
        cart.setUser(user());
        cart.setRestaurants(restaurant());
        return cart;
    }

    @Test
    void getOrCreateCart_createsNewWhenNoneExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant()));
        when(cartRepository.findByUserIdAndRestaurantsId(1L, 2L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Carts.class))).thenAnswer(inv -> {
            Carts c = inv.getArgument(0);
            c.setId(9L);
            return c;
        });

        CartResponse response = cartService.getOrCreateCart(1L, 2L);

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRestaurantName()).isEqualTo("Khmer Kitchen");
    }

    @Test
    void addItem_createsNewItemWithSubTotal() {
        Carts cart = cart();
        when(cartRepository.findById(9L)).thenReturn(Optional.of(cart));
        when(foodRepository.findById(3L)).thenReturn(Optional.of(food()));
        when(cartItemRepository.findByCartIdAndFoodsId(9L, 3L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItems.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItemRequest request = new CartItemRequest();
        request.setFoodId(3L);
        request.setQuantity(3);

        CartResponse response = cartService.addItem(9L, request);

        assertThat(response.getItems()).hasSize(1);
        CartItemResponse item = response.getItems().get(0);
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getSubTotal()).isEqualByComparingTo("30.00");
        assertThat(response.getTotalPrice()).isEqualByComparingTo("30.00");
    }

    @Test
    void addItem_rejectsFoodFromAnotherRestaurant() {
        Carts cart = cart();
        Foods food = food();
        Restaurants otherRestaurant = new Restaurants();
        otherRestaurant.setId(99L);
        food.setRestaurants(otherRestaurant);

        when(cartRepository.findById(9L)).thenReturn(Optional.of(cart));
        when(foodRepository.findById(3L)).thenReturn(Optional.of(food));

        CartItemRequest request = new CartItemRequest();
        request.setFoodId(3L);
        request.setQuantity(1);

        assertThatThrownBy(() -> cartService.addItem(9L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");

        verify(cartItemRepository, never()).save(any());
    }
}
