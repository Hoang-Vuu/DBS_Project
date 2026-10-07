package com.example.demo.service;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderItemId;
import com.example.demo.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository repository;

    @InjectMocks
    private OrderItemService service;

    @Test
    void getOrderItemReturnsRepositoryResult() {
        OrderItemId id = new OrderItemId(1, 2);
        OrderItem item = new OrderItem();
        item.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(item));

        Optional<OrderItem> result = service.getOrderItem(id);

        assertThat(result).contains(item);
    }

    @Test
    void getOrderItemsByOrderIdReturnsFilteredItems() {
        OrderItem item = new OrderItem();
        item.setId(new OrderItemId(5, 8));
        when(repository.findByIdOrderId(5)).thenReturn(List.of(item));

        List<OrderItem> result = service.getOrderItemsByOrderId(5);

        assertThat(result).containsExactly(item);
    }

    @Test
    void deleteDelegatesToRepositoryWithCompositeKey() {
        OrderItemId id = new OrderItemId(3, 4);

        service.delete(id);

        verify(repository).deleteById(id);
    }
}
