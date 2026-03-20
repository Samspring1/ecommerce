package com.sam.ecommerce.repository;

import com.sam.ecommerce.model.Order;
import com.sam.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}