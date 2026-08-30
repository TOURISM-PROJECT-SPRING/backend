package com.example.spring_boot_project_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.spring_boot_project_api.dto.request.FoodOrderItemRequest;
import com.example.spring_boot_project_api.dto.request.FoodOrderRequest;
import com.example.spring_boot_project_api.dto.response.FoodOrderResponse;
import com.example.spring_boot_project_api.enums.OrderStatusEnum;
import com.example.spring_boot_project_api.model.CartItems;
import com.example.spring_boot_project_api.model.Carts;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.CartItemRepository;
import com.example.spring_boot_project_api.repository.CartRepository;
import com.example.spring_boot_project_api.repository.FoodOrderItemRepository;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.service.impl.FoodOrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class FoodOrderServiceImplTest {

    @Mock
    private FoodOrderRepository foodOrderRepository;
    @Mock
    private FoodOrderItemRepository foodOrderItemRepository;
    @Mock
    private FoodRepository foodRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private FoodOrderServiceImpl foodOrderService;

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

    private FoodOrderRequest request(LocalDateTime pickup) {
        FoodOrderItemRequest item = new FoodOrderItemRequest();
        item.setFoodId(3L);
        item.setQuantity(2);

        FoodOrderRequest request = new FoodOrderRequest();
        request.setUserId(1L);
        request.setRestaurantId(2L);
        request.setPickupTime(pickup);
        request.setItems(List.of(item));
        return request;
    }

    @Test
    void placeOrder_defaultsToPendingAndComputesTotal() {
        LocalDateTime pickup = LocalDateTime.now().plusHours(2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant()));
        when(foodRepository.findById(3L)).thenReturn(Optional.of(food()));
        when(foodOrderRepository.save(any(FoodOrders.class))).thenAnswer(inv -> {
            FoodOrders o = inv.getArgument(0);
            o.setId(7L);
            return o;
        });

        FoodOrderResponse response = foodOrderService.placeOrder(request(pickup));

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getStatus()).isEqualTo(OrderStatusEnum.PENDING.name());
        assertThat(response.getTotalPrice()).isEqualByComparingTo("20.00");
        assertThat(response.getPickupTime()).isEqualTo(pickup);
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void placeOrder_rejectsPastPickupTime() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant()));

        assertThatThrownBy(() -> foodOrderService.placeOrder(
                request(LocalDateTime.now().minusHours(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");

        verify(foodOrderRepository, never()).save(any());
    }

    @Test
    void placeOrder_rejectsUnavailableFood() {
        Foods food = food();
        food.setIsAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(restaurantRepository.findById(2L)).thenReturn(Optional.of(restaurant()));
        when(foodRepository.findById(3L)).thenReturn(Optional.of(food));

        assertThatThrownBy(() -> foodOrderService.placeOrder(
                request(LocalDateTime.now().plusHours(2))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");

        verify(foodOrderRepository, never()).save(any());
    }

    @Test
    void cancel_setsStatusToCancelled() {
        FoodOrders order = new FoodOrders();
        order.setId(1L);
        order.setStatus(OrderStatusEnum.PENDING.name());
        order.setUser(user());
        order.setRestuarants(restaurant());
        order.setPickupTime(LocalDateTime.now().plusHours(2));
        order.setFoodOrderItems(new ArrayList<>());

        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodOrderRepository.save(any(FoodOrders.class))).thenAnswer(inv -> inv.getArgument(0));

        FoodOrderResponse response = foodOrderService.cancel(1L);

        assertThat(response.getStatus()).isEqualTo(OrderStatusEnum.CANCELLED.name());
    }

    @Test
    void cancel_throwsWhenAlreadyCancelled() {
        FoodOrders order = new FoodOrders();
        order.setId(1L);
        order.setStatus(OrderStatusEnum.CANCELLED.name());

        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> foodOrderService.cancel(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already cancelled");

        verify(foodOrderRepository, never()).save(any());
    }

    @Test
    void updateStatus_rejectsUnknownStatus() {
        assertThatThrownBy(() -> foodOrderService.updateStatus(1L, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void placeOrderFromCart_convertsCartAndClearsIt() {
        Carts cart = new Carts();
        cart.setId(9L);
        cart.setUser(user());
        cart.setRestaurants(restaurant());

        CartItems cartItem = new CartItems();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setFoods(food());
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("10.00"));
        cartItem.setSubTotal(new BigDecimal("20.00"));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(restaurantRepository.existsById(2L)).thenReturn(true);
        when(cartRepository.findByUserIdAndRestaurantsId(1L, 2L))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(9L)).thenReturn(List.of(cartItem));
        when(foodOrderRepository.save(any(FoodOrders.class))).thenAnswer(inv -> {
            FoodOrders o = inv.getArgument(0);
            o.setId(7L);
            return o;
        });

        FoodOrderResponse response = foodOrderService.placeOrderFromCart(
                1L, 2L, LocalDateTime.now().plusHours(2));

        assertThat(response.getTotalPrice()).isEqualByComparingTo("20.00");
        assertThat(response.getItems()).hasSize(1);
        verify(cartItemRepository).deleteByCartId(9L);
    }

    @Test
    void placeOrderFromCart_throwsOnEmptyCart() {
        Carts cart = new Carts();
        cart.setId(9L);
        cart.setUser(user());
        cart.setRestaurants(restaurant());

        when(userRepository.existsById(1L)).thenReturn(true);
        when(restaurantRepository.existsById(2L)).thenReturn(true);
        when(cartRepository.findByUserIdAndRestaurantsId(1L, 2L))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(9L)).thenReturn(List.of());

        assertThatThrownBy(() -> foodOrderService.placeOrderFromCart(
                1L, 2L, LocalDateTime.now().plusHours(2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");

        verify(foodOrderRepository, never()).save(any());
    }
}
