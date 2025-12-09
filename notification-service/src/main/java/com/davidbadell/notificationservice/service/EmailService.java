package com.davidbadell.notificationservice.service;

import com.davidbadell.notificationservice.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${notification.email.from:noreply@davidbadell.com}")
    private String fromEmail;
    
    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;
    
    public void sendEmail(NotificationRequest request) {
        if (!emailEnabled) {
            log.info("Email sending disabled. Would send email to: {} with subject: {}", 
                    request.getTo(), request.getSubject());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", request.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", request.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendOrderConfirmationEmail(String to, String orderNumber, String totalAmount) {
        NotificationRequest request = NotificationRequest.builder()
                .to(to)
                .subject("Order Confirmation - " + orderNumber)
                .body(buildOrderConfirmationBody(orderNumber, totalAmount))
                .type(NotificationRequest.NotificationType.EMAIL)
                .build();
        
        sendEmail(request);
    }
    
    public void sendOrderStatusUpdateEmail(String to, String orderNumber, String status) {
        NotificationRequest request = NotificationRequest.builder()
                .to(to)
                .subject("Order Update - " + orderNumber)
                .body(buildOrderStatusUpdateBody(orderNumber, status))
                .type(NotificationRequest.NotificationType.EMAIL)
                .build();
        
        sendEmail(request);
    }
    
    private String buildOrderConfirmationBody(String orderNumber, String totalAmount) {
        return String.format("""
            Dear Customer,
            
            Thank you for your order!
            
            Order Number: %s
            Total Amount: $%s
            
            We will notify you when your order is shipped.
            
            Best regards,
            The Team
            """, orderNumber, totalAmount);
    }
    
    private String buildOrderStatusUpdateBody(String orderNumber, String status) {
        return String.format("""
            Dear Customer,
            
            Your order status has been updated.
            
            Order Number: %s
            New Status: %s
            
            Thank you for shopping with us!
            
            Best regards,
            The Team
            """, orderNumber, status);
    }
}
