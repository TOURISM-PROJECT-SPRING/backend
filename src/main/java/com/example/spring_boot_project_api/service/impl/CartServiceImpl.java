package com.example.spring_boot_project_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.dto.request.CartItemRequest;
import com.example.spring_boot_project_api.dto.response.CartItemResponse;
import com.example.spring_boot_project_api.dto.response.CartResponse;
import com.example.spring_boot_project_api.exception.ResourceNotFoundException;
import com.example.spring_boot_project_api.mapper.CartMapper;
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
import com.example.spring_boot_project_api.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getOrCreateCart(Long userId, Long restaurantId) {
        return CartMapper.toResponse(resolveCart(userId, restaurantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> findCartsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return CartMapper.toResponseList(cartRepository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> findCartItems(Long cartId) {
        Carts cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));
        return CartMapper.toItemResponseList(cartItemRepository.findByCartId(cart.getId()));
    }

    @Override
    public CartResponse addItem(Long cartId, CartItemRequest request) {
        Carts cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));

        Foods food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food", request.getFoodId()));

        if (Boolean.FALSE.equals(food.getIsAvailable())) {
            throw new IllegalArgumentException("Food is not available: " + food.getName());
        }

        if (!food.getRestaurants().getId().equals(cart.getRestaurants().getId())) {
            throw new IllegalArgumentException("Food does not belong to this cart's restaurant");
        }

        CartItems item = cartItemRepository
                .findByCartIdAndFoodsId(cart.getId(), food.getId())
                .orElseGet(() -> CartMapper.toCartItem(cart, food, request.getQuantity()));

        if (item.getId() != null) {
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setSubTotal(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
        } else {
            cart.getCartItems().add(item);
        }

        cartItemRepository.save(item);
        return CartMapper.toResponse(reload(cart));
    }

    @Override
    public CartResponse updateItemQuantity(Long cartId, Long itemId, Integer quantity) {
        Carts cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));

        CartItems item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to this cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            item.setSubTotal(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(quantity)));
            cartItemRepository.save(item);
        }

        return CartMapper.toResponse(reload(cart));
    }

    @Override
    public CartResponse removeItem(Long cartId, Long itemId) {
        Carts cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));

        CartItems item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to this cart");
        }

        cartItemRepository.delete(item);
        return CartMapper.toResponse(reload(cart));
    }

    @Override
    public CartResponse clearCart(Long cartId) {
        Carts cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));
        cartItemRepository.deleteByCartId(cart.getId());
        return CartMapper.toResponse(reload(cart));
    }

    private Carts resolveCart(Long userId, Long restaurantId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Restaurants restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));
        return cartRepository.findByUserIdAndRestaurantsId(userId, restaurantId)
                .orElseGet(() -> cartRepository.save(CartMapper.toEntity(user, restaurant)));
    }

    private Carts reload(Carts cart) {
        return cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cart.getId()));
    }
}
