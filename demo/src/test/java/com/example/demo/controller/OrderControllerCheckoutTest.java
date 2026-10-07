package com.example.demo.controller;

import com.example.demo.dto.order.CreateOrderResponse;
import com.example.demo.dto.order.CreateOrderResponseItem;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerCheckoutTest {

    private final OrderService service = mock(OrderService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void checkoutReturnsCreatedOnSuccess() throws Exception {
        CreateOrderResponse response = new CreateOrderResponse(
                10,
                1,
                LocalDateTime.parse("2026-10-07T10:30:00"),
                LocalDateTime.parse("2026-10-10T10:30:00"),
                2,
                "PENDING",
                List.of(new CreateOrderResponseItem(3, 2, new BigDecimal("14.50"))));

        when(service.createOrderWithItems(any())).thenReturn(response);

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "shippingAddressId": 2,
                                  "deliveryDate": "2026-10-10T10:30:00",
                                  "items": [
                                    { "productId": 3, "quantity": 2 }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/10"))
                .andExpect(jsonPath("$.orderId").value(10));
    }

    @Test
    void checkoutReturnsBadRequestWhenValidationFails() throws Exception {
        when(service.createOrderWithItems(any())).thenThrow(new BadRequestException("invalid"));

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid"));
    }

    @Test
    void checkoutReturnsNotFoundWhenCustomerOrProductMissing() throws Exception {
        when(service.createOrderWithItems(any())).thenThrow(new ResourceNotFoundException("missing"));

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("missing"));
    }

    @Test
    void checkoutReturnsConflictForDuplicateOrInsufficientStock() throws Exception {
        when(service.createOrderWithItems(any())).thenThrow(new ConflictException("conflict"));

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("conflict"));
    }
}
