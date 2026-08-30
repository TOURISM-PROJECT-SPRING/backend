package com.example.spring_boot_project_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.FoodOrderItemRequest;
import com.example.spring_boot_project_api.dto.request.FoodOrderRequest;
import com.example.spring_boot_project_api.dto.response.FoodOrderResponse;
import com.example.spring_boot_project_api.enums.OrderStatusEnum;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.FoodOrderMapper;
import com.example.spring_boot_project_api.model.CartItems;
import com.example.spring_boot_project_api.model.Carts;
import com.example.spring_boot_project_api.model.FoodOrderItems;
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
import com.example.spring_boot_project_api.service.FoodOrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodOrderServiceImpl implements FoodOrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final FoodOrderItemRepository foodOrderItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> findAll() {
        return FoodOrderMapper.toResponseList(foodOrderRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public FoodOrderResponse findById(Long id) {
        FoodOrders order = foodOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Order", id));
        return FoodOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return FoodOrderMapper.toResponseList(foodOrderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> findByRestaurantId(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }
        return FoodOrderMapper.toResponseList(foodOrderRepository.findByRestuarantsId(restaurantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> findByStatus(String status) {
        return FoodOrderMapper.toResponseList(
                foodOrderRepository.findByStatus(FoodOrderMapper.normalizeStatus(status)));
    }

    @Override
    public FoodOrderResponse placeOrder(FoodOrderRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Restaurants restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant", request.getRestaurantId()));

        validatePickupTime(request.getPickupTime());

        FoodOrders order = new FoodOrders();
        order.setUser(user);
        order.setRestuarants(restaurant);
        order.setPickupTime(request.getPickupTime());
        order.setStatus(OrderStatusEnum.PENDING.name());

        BigDecimal total = BigDecimal.ZERO;
        for (FoodOrderItemRequest itemRequest : request.getItems()) {
            Foods food = foodRepository.findById(itemRequest.getFoodId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Food", itemRequest.getFoodId()));
            validateFood(order, food);

            FoodOrderItems item = FoodOrderMapper.toOrderItem(
                    order, food, itemRequest.getQuantity());
            order.getFoodOrderItems().add(item);
            total = total.add(item.getSubTotal());
        }

        order.setTotalPrice(total);
        FoodOrders saved = foodOrderRepository.save(order);
        return FoodOrderMapper.toResponse(saved);
    }

    @Override
    public FoodOrderResponse placeOrderFromCart(Long userId, Long restaurantId,
                                                LocalDateTime pickupTime) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }
        validatePickupTime(pickupTime);

        Carts cart = cartRepository.findByUserIdAndRestaurantsId(userId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart for user and restaurant"));

        List<CartItems> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order from an empty cart");
        }

        FoodOrders order = new FoodOrders();
        order.setUser(cart.getUser());
        order.setRestuarants(cart.getRestaurants());
        order.setPickupTime(pickupTime);
        order.setStatus(OrderStatusEnum.PENDING.name());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItems cartItem : cartItems) {
            FoodOrderItems item = FoodOrderMapper.toOrderItem(
                    order, cartItem.getFoods(), cartItem.getQuantity());
            order.getFoodOrderItems().add(item);
            total = total.add(item.getSubTotal());
        }

        order.setTotalPrice(total);
        FoodOrders saved = foodOrderRepository.save(order);

        cartItemRepository.deleteByCartId(cart.getId());
        return FoodOrderMapper.toResponse(saved);
    }

    @Override
    public FoodOrderResponse updateStatus(Long id, String status) {
        String normalized = FoodOrderMapper.normalizeStatus(status);
        if (!OrderStatusEnum.isValid(normalized)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        FoodOrders order = foodOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Order", id));

        if (OrderStatusEnum.CANCELLED.name().equals(normalized)
                && FoodOrderMapper.isCancelled(order)) {
            throw new IllegalArgumentException("Order is already cancelled");
        }

        order.setStatus(normalized);
        FoodOrders saved = foodOrderRepository.save(order);
        return FoodOrderMapper.toResponse(saved);
    }

    @Override
    public FoodOrderResponse cancel(Long id) {
        FoodOrders order = foodOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Order", id));

        if (FoodOrderMapper.isCancelled(order)) {
            throw new IllegalArgumentException("Order is already cancelled");
        }
        if (OrderStatusEnum.COMPLETED.name().equalsIgnoreCase(order.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel a completed order");
        }

        order.setStatus(OrderStatusEnum.CANCELLED.name());
        FoodOrders saved = foodOrderRepository.save(order);
        return FoodOrderMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!foodOrderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Food Order", id);
        }
        foodOrderRepository.deleteById(id);
    }

    private void validatePickupTime(LocalDateTime pickupTime) {
        if (pickupTime == null || pickupTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Pickup time must be in the future");
        }
    }

    private void validateFood(FoodOrders order, Foods food) {
        if (Boolean.FALSE.equals(food.getIsAvailable())) {
            throw new IllegalArgumentException("Food is not available: " + food.getName());
        }
        if (!food.getRestaurants().getId().equals(order.getRestuarants().getId())) {
            throw new IllegalArgumentException(
                    "Food " + food.getName() + " does not belong to the selected restaurant");
        }
    }
}
