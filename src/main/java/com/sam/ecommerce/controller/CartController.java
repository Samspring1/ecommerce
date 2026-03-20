package com.sam.ecommerce.controller;

import com.sam.ecommerce.dto.CartItemDto;
import com.sam.ecommerce.model.Order;
import com.sam.ecommerce.model.Product;
import com.sam.ecommerce.model.User;
import com.sam.ecommerce.repository.ProductRepository;
import com.sam.ecommerce.repository.UserRepository;
import com.sam.ecommerce.security.UserDetailsImpl;
import com.sam.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    // In-memory cart storage (for demonstration)
    private final ConcurrentHashMap<Long, List<CartItemDto>> userCarts = new ConcurrentHashMap<>();

    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCart(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<CartItemDto> cart = userCarts.getOrDefault(currentUser.getId(), new ArrayList<>());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                       @PathVariable Long productId,
                                       @RequestParam(defaultValue = "1") Integer quantity) {

        if (quantity <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<CartItemDto> cart = userCarts.computeIfAbsent(currentUser.getId(), k -> new ArrayList<>());

        CartItemDto existingItem = cart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setTotalPrice(existingItem.getQuantity() * existingItem.getPrice());
        } else {
            CartItemDto newItem = new CartItemDto();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice().doubleValue());
            newItem.setTotalPrice(quantity * product.getPrice().doubleValue());
            cart.add(newItem);
        }

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                            @PathVariable Long productId) {
        List<CartItemDto> cart = userCarts.get(currentUser.getId());
        if (cart != null) {
            cart.removeIf(item -> item.getProductId().equals(productId));
        }
        return ResponseEntity.ok("Item removed from cart");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        userCarts.remove(currentUser.getId());
        return ResponseEntity.ok("Cart cleared");
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<CartItemDto> cart = userCarts.get(currentUser.getId());

        if (cart == null || cart.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        try {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderService.createOrder(user, cart);

            userCarts.remove(currentUser.getId());

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Checkout failed: " + e.getMessage());
        }
    }
}