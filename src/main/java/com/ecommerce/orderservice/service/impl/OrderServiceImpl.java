package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.client.NotificationClient;
import com.ecommerce.orderservice.dto.NotificationRequestDto;
import com.ecommerce.orderservice.dto.OrderItemDto;
import com.ecommerce.orderservice.dto.OrderRequestDto;
import com.ecommerce.orderservice.dto.OrderResponseDto;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderItem;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final NotificationClient notificationClient;

    public OrderServiceImpl(OrderRepository orderRepository, NotificationClient notificationClient) {
        this.orderRepository = orderRepository;
        this.notificationClient = notificationClient;
    }

    @Override
    public OrderResponseDto createOrder(OrderRequestDto orderRequest) {
        log.info("Processing new order creation for customer: {}", orderRequest.getCustomerEmail());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (OrderItemDto itemDto : orderRequest.getItems()) {
            BigDecimal subtotal = itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            OrderItem item = new OrderItem(
                    itemDto.getProductId(),
                    itemDto.getProductName(),
                    itemDto.getQuantity(),
                    itemDto.getPrice(),
                    subtotal
            );
            items.add(item);
            calculatedTotal = calculatedTotal.add(subtotal);
        }

        Order order = new Order();
        order.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setItems(items);
        order.setTotalAmount(calculatedTotal);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CONFIRMED);

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved successfully with ID: {}", savedOrder.getOrderId());

        // Call notification service via RestClient
        NotificationRequestDto notification = new NotificationRequestDto(
                savedOrder.getOrderId(),
                savedOrder.getCustomerEmail(),
                "Order Confirmation - " + savedOrder.getOrderId(),
                String.format("Thank you for your order! Your order %s of total $%s has been placed successfully.",
                        savedOrder.getOrderId(), savedOrder.getTotalAmount()),
                savedOrder.getTotalAmount()
        );
        notificationClient.sendOrderConfirmation(notification);

        return mapToDto(savedOrder);
    }

    @Override
    public List<OrderResponseDto> getAllOrders(String customerEmail) {
        log.info("Retrieving orders with filter customerEmail: {}", customerEmail);
        List<Order> orders;
        if (StringUtils.hasText(customerEmail)) {
            orders = orderRepository.findByCustomerEmailIgnoreCase(customerEmail.trim());
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getOrderById(String orderId) {
        log.info("Retrieving order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToDto(order);
    }

    private OrderResponseDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new OrderResponseDto(
                order.getOrderId(),
                order.getCustomerEmail(),
                itemDtos,
                order.getTotalAmount(),
                order.getOrderDate(),
                order.getStatus()
        );
    }
}
