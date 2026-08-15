package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.NotificationRequestDto;

public interface NotificationClient {

    void sendOrderConfirmation(NotificationRequestDto notificationRequest);

}
