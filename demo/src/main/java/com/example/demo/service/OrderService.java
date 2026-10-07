package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    public Order getOrder(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public Order save(Order order) {
        return repository.save(order);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}