package com.example.demo.dto.order;

import java.math.BigDecimal;

public class CreateOrderResponseItem {

    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public CreateOrderResponseItem(Integer productId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
