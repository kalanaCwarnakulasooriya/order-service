package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.NotificationClient;
import com.ecommerce.orderservice.dto.NotificationRequestDto;
import com.ecommerce.orderservice.dto.OrderItemDto;
import com.ecommerce.orderservice.dto.OrderRequestDto;
import com.ecommerce.orderservice.dto.OrderResponseDto;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationClient notificationClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, notificationClient);
    }

    @Test
    @DisplayName("createOrder should calculate item subtotals, compute totalAmount, persist order, and send notification")
    void shouldCreateOrderAndCalculateTotals() {
        OrderItemDto item1 = new OrderItemDto("P1", "Laptop", 2, new BigDecimal("1000.00"), null);
        OrderItemDto item2 = new OrderItemDto("P2", "Mouse", 3, new BigDecimal("50.00"), null);
        OrderRequestDto requestDto = new OrderRequestDto("customer@example.com", List.of(item1, item2));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto response = orderService.createOrder(requestDto);

        assertNotNull(response);
        assertEquals("customer@example.com", response.getCustomerEmail());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("2150.00"), response.getTotalAmount());
        assertEquals(2, response.getItems().size());
        assertEquals(new BigDecimal("2000.00"), response.getItems().get(0).getSubtotal());
        assertEquals(new BigDecimal("150.00"), response.getItems().get(1).getSubtotal());

        // Verify repository interaction
        verify(orderRepository, times(1)).save(any(Order.class));

        // Verify notification dispatch
        ArgumentCaptor<NotificationRequestDto> notifCaptor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient, times(1)).sendOrderConfirmation(notifCaptor.capture());
        NotificationRequestDto sentNotif = notifCaptor.getValue();
        assertEquals("customer@example.com", sentNotif.getRecipientEmail());
        assertEquals(new BigDecimal("2150.00"), sentNotif.getTotalAmount());
    }

    @Test
    @DisplayName("getOrderById should return existing order")
    void shouldGetOrderById() {
        Order order = new Order("ORD-1", "user@test.com", List.of(), new BigDecimal("100.00"), null, OrderStatus.CONFIRMED);
        when(orderRepository.findById("ORD-1")).thenReturn(Optional.of(order));

        OrderResponseDto response = orderService.getOrderById("ORD-1");

        assertNotNull(response);
        assertEquals("ORD-1", response.getOrderId());
        assertEquals("user@test.com", response.getCustomerEmail());
    }
}
