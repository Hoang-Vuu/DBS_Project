package com.example.demo.dto.order;

import java.time.LocalDateTime;
import java.util.List;

public class CreateOrderResponse {

    private Integer orderId;
    private Integer customerId;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private Integer shippingAddressId;
    private String status;
    private List<CreateOrderResponseItem> items;

    public CreateOrderResponse(
            Integer orderId,
            Integer customerId,
            LocalDateTime orderDate,
            LocalDateTime deliveryDate,
            Integer shippingAddressId,
            String status,
            List<CreateOrderResponseItem> items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.shippingAddressId = shippingAddressId;
        this.status = status;
        this.items = items;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public Integer getShippingAddressId() {
        return shippingAddressId;
    }

    public String getStatus() {
        return status;
    }

    public List<CreateOrderResponseItem> getItems() {
        return items;
    }
}
