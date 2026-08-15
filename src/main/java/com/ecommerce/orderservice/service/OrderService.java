package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderRequestDto;
import com.ecommerce.orderservice.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto orderRequest);

    List<OrderResponseDto> getAllOrders(String customerEmail);

    OrderResponseDto getOrderById(String orderId);

}
