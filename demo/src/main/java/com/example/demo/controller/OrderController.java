package com.example.demo.controller;

import com.example.demo.dto.order.CreateOrderRequest;
import com.example.demo.dto.order.CreateOrderResponse;
import com.example.demo.entity.Order;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<Order> getAll() {
        return service.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable Integer id) {
        return service.getOrder(id);
    }

    @PostMapping
    public Order create(@RequestBody Order order) {
        return service.save(order);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CreateOrderResponse> checkout(@RequestBody CreateOrderRequest request) {
        CreateOrderResponse created = service.createOrderWithItems(request);
        return ResponseEntity
                .created(URI.create("/orders/" + created.getOrderId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public Order update(
            @PathVariable Integer id,
            @RequestBody Order order) {

        order.setOrderId(id);
        return service.save(order);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
