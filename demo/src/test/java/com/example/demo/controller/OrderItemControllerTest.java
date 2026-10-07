package com.example.demo.controller;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderItemId;
import com.example.demo.service.OrderItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderItemControllerTest {

    private final OrderItemService service = mock(OrderItemService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderItemController(service)).build();
    }

    @Test
    void getByIdReturnsNotFoundWhenMissing() throws Exception {
        when(service.getOrderItem(new OrderItemId(10, 20))).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/10/items/20"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUsesPathOrderIdEvenWhenBodyContainsDifferentOrderId() throws Exception {
        OrderItem saved = new OrderItem();
        saved.setId(new OrderItemId(10, 3));
        saved.setQuantity(2);
        saved.setUnitPrice(new BigDecimal("14.50"));

        when(service.save(any(OrderItem.class))).thenReturn(saved);

        mockMvc.perform(post("/orders/10/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": {
                                    "orderId": 99,
                                    "productId": 3
                                  },
                                  "quantity": 2,
                                  "unitPrice": 14.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.orderId").value(10))
                .andExpect(jsonPath("$.id.productId").value(3));

        verify(service).save(argThat(item ->
                item.getId() != null
                        && item.getId().getOrderId().equals(10)
                        && item.getId().getProductId().equals(3)));
    }

    @Test
    void createReturnsBadRequestWhenProductIdMissing() throws Exception {
        mockMvc.perform(post("/orders/10/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReturnsNotFoundWhenTargetMissing() throws Exception {
        when(service.getOrderItem(new OrderItemId(7, 8))).thenReturn(Optional.empty());

        mockMvc.perform(put("/orders/7/items/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": {
                                    "orderId": 1,
                                    "productId": 1
                                  }
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsNotFoundWhenTargetMissing() throws Exception {
        when(service.getOrderItem(new OrderItemId(7, 8))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/orders/7/items/8"))
                .andExpect(status().isNotFound());
    }
}
