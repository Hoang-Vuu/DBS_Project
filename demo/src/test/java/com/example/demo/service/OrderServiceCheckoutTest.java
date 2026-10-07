package com.example.demo.service;

import com.example.demo.dto.order.CreateOrderItemRequest;
import com.example.demo.dto.order.CreateOrderRequest;
import com.example.demo.dto.order.CreateOrderResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceCheckoutTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService service;

    @Test
    void createOrderWithItemsSucceedsForMultipleItems() {
        CreateOrderRequest request = request(1,
                item(10, 2),
                item(20, 1));

        Product product10 = product(10, 5, 14.50);
        Product product20 = product(20, 8, 9.99);

        when(customerRepository.existsById(1)).thenReturn(true);
        when(productRepository.findByIdForUpdate(10)).thenReturn(java.util.Optional.of(product10));
        when(productRepository.findByIdForUpdate(20)).thenReturn(java.util.Optional.of(product20));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setOrderId(77);
            return saved;
        });
        when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderResponse response = service.createOrderWithItems(request);

        assertThat(response.getOrderId()).isEqualTo(77);
        assertThat(response.getCustomerId()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getProductId()).isEqualTo(10);
        assertThat(response.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("14.5"));

        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(any());
    }

    @Test
    void createOrderWithItemsFailsWhenCustomerMissing() {
        CreateOrderRequest request = request(99, item(10, 1));
        when(customerRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.createOrderWithItems(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).saveAll(any());
    }

    @Test
    void createOrderWithItemsFailsWhenProductMissing() {
        CreateOrderRequest request = request(1, item(999, 1));
        when(customerRepository.existsById(1)).thenReturn(true);
        when(productRepository.findByIdForUpdate(999)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.createOrderWithItems(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderWithItemsFailsWhenQuantityNotPositive() {
        CreateOrderRequest request = request(1, item(10, 0));
        when(customerRepository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> service.createOrderWithItems(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("quantity must be positive");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderWithItemsFailsOnDuplicateProductIds() {
        CreateOrderRequest request = request(1, item(10, 1), item(10, 2));
        when(customerRepository.existsById(1)).thenReturn(true);

        Product product10 = product(10, 10, 12.00);
        when(productRepository.findByIdForUpdate(10)).thenReturn(java.util.Optional.of(product10));

        assertThatThrownBy(() -> service.createOrderWithItems(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Duplicate productId");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderWithItemsFailsWhenStockInsufficientAndDoesNotPersist() {
        CreateOrderRequest request = request(1, item(10, 5));
        when(customerRepository.existsById(1)).thenReturn(true);
        when(productRepository.findByIdForUpdate(10)).thenReturn(java.util.Optional.of(product(10, 2, 4.50)));

        assertThatThrownBy(() -> service.createOrderWithItems(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).saveAll(any());
    }

    @Test
    void createOrderWithItemsUsesProductPriceSnapshot() {
        CreateOrderRequest request = request(1, item(10, 2));
        when(customerRepository.existsById(1)).thenReturn(true);
        when(productRepository.findByIdForUpdate(10)).thenReturn(java.util.Optional.of(product(10, 7, 21.75)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setOrderId(55);
            return saved;
        });
        when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createOrderWithItems(request);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());

        OrderItem capturedItem = itemsCaptor.getValue().get(0);
        assertThat(capturedItem.getUnitPrice()).isEqualByComparingTo(new BigDecimal("21.75"));
    }

    private static CreateOrderRequest request(Integer customerId, CreateOrderItemRequest... items) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setShippingAddressId(5);
        request.setDeliveryDate(LocalDateTime.parse("2026-10-10T10:00:00"));
        request.setItems(List.of(items));
        return request;
    }

    private static CreateOrderItemRequest item(Integer productId, Integer quantity) {
        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private static Product product(Integer productId, Integer stock, Double price) {
        Product product = new Product();
        product.setProductId(productId);
        product.setStockQuantity(stock);
        product.setPrice(price);
        return product;
    }
}
