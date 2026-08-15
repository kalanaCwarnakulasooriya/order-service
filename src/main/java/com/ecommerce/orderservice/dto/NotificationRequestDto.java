package com.ecommerce.orderservice.dto;

import java.math.BigDecimal;

public class NotificationRequestDto {

    private String orderId;
    private String recipientEmail;
    private String subject;
    private String message;
    private BigDecimal totalAmount;

    public NotificationRequestDto() {
    }

    public NotificationRequestDto(String orderId, String recipientEmail, String subject, String message, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
