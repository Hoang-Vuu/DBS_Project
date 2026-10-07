package com.example.demo.repository;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, OrderItemId> {

    List<OrderItem> findByIdOrderId(Integer orderId);
}
