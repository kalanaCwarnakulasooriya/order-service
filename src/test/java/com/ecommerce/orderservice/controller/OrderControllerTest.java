package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderItemDto;
import com.ecommerce.orderservice.dto.OrderRequestDto;
import com.ecommerce.orderservice.dto.OrderResponseDto;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders should create order and return 201 Created")
    void shouldCreateOrder() throws Exception {
        OrderItemDto itemDto = new OrderItemDto("PROD-1", "Mechanical Keyboard", 2, new BigDecimal("89.99"), new BigDecimal("179.98"));
        OrderRequestDto requestDto = new OrderRequestDto("john.doe@example.com", List.of(itemDto));

        OrderResponseDto responseDto = new OrderResponseDto(
                "ORD-12345678",
                "john.doe@example.com",
                List.of(itemDto),
                new BigDecimal("179.98"),
                LocalDateTime.now(),
                OrderStatus.CONFIRMED
        );

        when(orderService.createOrder(any(OrderRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORD-12345678"))
                .andExpect(jsonPath("$.customerEmail").value("john.doe@example.com"))
                .andExpect(jsonPath("$.totalAmount").value(179.98))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.items[0].productName").value("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("GET /api/v1/orders should return list of orders")
    void shouldReturnAllOrders() throws Exception {
        OrderItemDto itemDto = new OrderItemDto("PROD-1", "Mouse", 1, new BigDecimal("25.00"), new BigDecimal("25.00"));
        OrderResponseDto order = new OrderResponseDto(
                "ORD-9999", "customer@example.com", List.of(itemDto), new BigDecimal("25.00"), LocalDateTime.now(), OrderStatus.CONFIRMED
        );

        when(orderService.getAllOrders(null)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value("ORD-9999"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} should return order by ID")
    void shouldReturnOrderById() throws Exception {
        OrderItemDto itemDto = new OrderItemDto("PROD-2", "Monitor", 1, new BigDecimal("299.99"), new BigDecimal("299.99"));
        OrderResponseDto order = new OrderResponseDto(
                "ORD-7777", "customer@example.com", List.of(itemDto), new BigDecimal("299.99"), LocalDateTime.now(), OrderStatus.CONFIRMED
        );

        when(orderService.getOrderById("ORD-7777")).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/ORD-7777"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-7777"))
                .andExpect(jsonPath("$.totalAmount").value(299.99));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} should return 404 when order not found")
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(orderService.getOrderById("ORD-NONEXISTENT"))
                .thenThrow(new ResourceNotFoundException("Order not found with id: ORD-NONEXISTENT"));

        mockMvc.perform(get("/api/v1/orders/ORD-NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: ORD-NONEXISTENT"));
    }
}
