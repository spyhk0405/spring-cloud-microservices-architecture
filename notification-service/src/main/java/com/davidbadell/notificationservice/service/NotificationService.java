package com.davidbadell.notificationservice.service;

import com.davidbadell.notificationservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final EmailService emailService;
    
    @RabbitListener(queues = "${rabbitmq.queue.notification:notification-queue}")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for order: {}", event.getEventType(), event.getOrderNumber());
        
        try {
            switch (event.getEventType()) {
                case "ORDER_CREATED" -> handleOrderCreated(event);
                case "ORDER_STATUS_UPDATED" -> handleOrderStatusUpdated(event);
                case "ORDER_CANCELLED" -> handleOrderCancelled(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing order event: {}", event, e);
            throw e;
        }
    }
    
    private void handleOrderCreated(OrderEvent event) {
        log.info("Processing ORDER_CREATED event for order: {}", event.getOrderNumber());
        
        // In a real application, we would fetch the user email from user service
        String userEmail = "customer@example.com"; // Placeholder
        
        // Send confirmation email (would be sent in production)
        log.info("Order confirmation notification sent for order: {}", event.getOrderNumber());
    }
    
    private void handleOrderStatusUpdated(OrderEvent event) {
        log.info("Processing ORDER_STATUS_UPDATED event for order: {} with status: {}", 
                event.getOrderNumber(), event.getStatus());
        
        // Send status update notification
        log.info("Order status update notification sent for order: {}", event.getOrderNumber());
    }
    
    private void handleOrderCancelled(OrderEvent event) {
        log.info("Processing ORDER_CANCELLED event for order: {}", event.getOrderNumber());
        
        // Send cancellation notification
        log.info("Order cancellation notification sent for order: {}", event.getOrderNumber());
    }
}
