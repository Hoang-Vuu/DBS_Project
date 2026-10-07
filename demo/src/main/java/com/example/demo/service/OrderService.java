package com.example.demo.service;

import com.example.demo.dto.order.CreateOrderItemRequest;
import com.example.demo.dto.order.CreateOrderRequest;
import com.example.demo.dto.order.CreateOrderResponse;
import com.example.demo.dto.order.CreateOrderResponseItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderItemId;
import com.example.demo.entity.Product;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final String DEFAULT_ORDER_STATUS = "PENDING";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public CreateOrderResponse createOrderWithItems(CreateOrderRequest request) {
        validateCreateOrderRequest(request);

        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new ResourceNotFoundException("Customer not found: " + request.getCustomerId());
        }

        Map<Integer, Product> lockedProducts = loadAndValidateProducts(request.getItems());

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setShippingAddressId(request.getShippingAddressId());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now());
        order.setStatus(normalizeStatus(request.getStatus()));

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            Product product = lockedProducts.get(itemRequest.getProductId());
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());

            OrderItem item = new OrderItem();
            item.setId(new OrderItemId(savedOrder.getOrderId(), itemRequest.getProductId()));
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            orderItems.add(item);
        }

        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);

        return mapCreateOrderResponse(savedOrder, savedItems);
    }

    public void delete(Integer id) {
        orderRepository.deleteById(id);
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getCustomerId() == null) {
            throw new BadRequestException("customerId is required");
        }
        if (request.getShippingAddressId() == null) {
            throw new BadRequestException("shippingAddressId is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("items must contain at least one order item");
        }
    }

    private Map<Integer, Product> loadAndValidateProducts(List<CreateOrderItemRequest> items) {
        Map<Integer, Product> productsById = new LinkedHashMap<>();

        for (CreateOrderItemRequest item : items) {
            if (item == null) {
                throw new BadRequestException("Order item must not be null");
            }
            if (item.getProductId() == null) {
                throw new BadRequestException("productId is required for each order item");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("quantity must be positive for product " + item.getProductId());
            }
            if (productsById.containsKey(item.getProductId())) {
                throw new ConflictException("Duplicate productId in order: " + item.getProductId());
            }

            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + item.getProductId()));

            if (product.getPrice() == null) {
                throw new ConflictException("Product has no price configured: " + item.getProductId());
            }

            Integer stock = product.getStockQuantity();
            if (stock == null || stock < item.getQuantity()) {
                throw new ConflictException("Insufficient stock for product " + item.getProductId());
            }

            productsById.put(item.getProductId(), product);
        }

        return productsById;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_ORDER_STATUS;
        }
        return status;
    }

    private CreateOrderResponse mapCreateOrderResponse(Order order, List<OrderItem> items) {
        List<CreateOrderResponseItem> responseItems = items.stream()
                .map(item -> new CreateOrderResponseItem(
                        item.getId().getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()))
                .toList();

        return new CreateOrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getOrderDate(),
                order.getDeliveryDate(),
                order.getShippingAddressId(),
                order.getStatus(),
                responseItems);
    }
}
