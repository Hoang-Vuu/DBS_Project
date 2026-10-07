package com.example.demo.controller;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderItemId;
import com.example.demo.service.OrderItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/orders/{orderId}/items")
public class OrderItemController {

    private final OrderItemService service;

    public OrderItemController(OrderItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderItem> getAllByOrder(@PathVariable Integer orderId) {
        return service.getOrderItemsByOrderId(orderId);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<OrderItem> getById(
            @PathVariable Integer orderId,
            @PathVariable Integer productId) {

        OrderItemId id = new OrderItemId(orderId, productId);
        return service.getOrderItem(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderItem> create(
            @PathVariable Integer orderId,
            @RequestBody OrderItem orderItem) {

        if (orderItem.getId() == null || orderItem.getId().getProductId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body must include id.productId");
        }

        orderItem.setId(new OrderItemId(orderId, orderItem.getId().getProductId()));
        OrderItem created = service.save(orderItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<OrderItem> update(
            @PathVariable Integer orderId,
            @PathVariable Integer productId,
            @RequestBody OrderItem orderItem) {

        OrderItemId id = new OrderItemId(orderId, productId);
        if (service.getOrderItem(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        orderItem.setId(id);
        return ResponseEntity.ok(service.save(orderItem));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer orderId,
            @PathVariable Integer productId) {

        OrderItemId id = new OrderItemId(orderId, productId);
        if (service.getOrderItem(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
