package com.example.demo.service;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderItemId;
import com.example.demo.repository.OrderItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemService {

    private final OrderItemRepository repository;

    public OrderItemService(OrderItemRepository repository) {
        this.repository = repository;
    }

    public List<OrderItem> getAllOrderItems() {
        return repository.findAll();
    }

    public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
        return repository.findByIdOrderId(orderId);
    }

    public Optional<OrderItem> getOrderItem(OrderItemId id) {
        return repository.findById(id);
    }

    @Transactional
    public OrderItem save(OrderItem orderItem) {
        return repository.save(orderItem);
    }

    @Transactional
    public void delete(OrderItemId id) {
        repository.deleteById(id);
    }
}
