package com.ecommerce.orderservice.client.impl;

import com.ecommerce.orderservice.client.NotificationClient;
import com.ecommerce.orderservice.dto.NotificationRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClientImpl implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientImpl.class);

    private final RestClient restClient;
    private final String notificationServiceUrl;

    public NotificationClientImpl(RestClient.Builder restClientBuilder,
                                  @Value("${notification.service.url:http://notification-service/api/v1/notifications}") String notificationServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.notificationServiceUrl = notificationServiceUrl;
    }

    @Override
    public void sendOrderConfirmation(NotificationRequestDto notificationRequest) {
        log.info("Dispatching order confirmation notification for orderId: {} to {}",
                notificationRequest.getOrderId(), notificationServiceUrl);
        try {
            restClient.post()
                    .uri(notificationServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(notificationRequest)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully sent notification for orderId: {}", notificationRequest.getOrderId());
        } catch (Exception ex) {
            log.warn("Failed to deliver notification for orderId: {}. Reason: {}",
                    notificationRequest.getOrderId(), ex.getMessage());
        }
    }
}
