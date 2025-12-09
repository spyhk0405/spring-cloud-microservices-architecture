package com.davidbadell.orderservice.service;

import com.davidbadell.orderservice.client.ProductClient;
import com.davidbadell.orderservice.dto.*;
import com.davidbadell.orderservice.entity.Order;
import com.davidbadell.orderservice.entity.OrderItem;
import com.davidbadell.orderservice.entity.OrderStatus;
import com.davidbadell.orderservice.event.OrderEvent;
import com.davidbadell.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.order:order-exchange}")
    private String orderExchange;
    
    @Value("${rabbitmq.routing-key.order-created:order.created}")
    private String orderCreatedRoutingKey;
    
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(request.getUserId())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .build();
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            ProductDTO product = productClient.getProductById(itemRequest.getProductId());
            
            if (product == null || !product.isActive()) {
                throw new RuntimeException("Product not found or inactive: " + itemRequest.getProductId());
            }
            
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            OrderItem item = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            order.addItem(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            // Reduce stock
            Boolean stockReduced = productClient.reduceStock(itemRequest.getProductId(), itemRequest.getQuantity());
            if (!stockReduced) {
                throw new RuntimeException("Failed to reduce stock for product: " + product.getName());
            }
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        // Publish order created event
        publishOrderEvent(savedOrder, "ORDER_CREATED");
        
        log.info("Order created with number: {}", savedOrder.getOrderNumber());
        return mapToResponse(savedOrder);
    }
    
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToResponse(order);
    }
    
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
        return mapToResponse(order);
    }
    
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }
    
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        // Publish order status update event
        publishOrderEvent(updatedOrder, "ORDER_STATUS_UPDATED");
        
        log.info("Order {} status updated to {}", order.getOrderNumber(), status);
        return mapToResponse(updatedOrder);
    }
    
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel order that has been shipped or delivered");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        
        // Publish order cancelled event
        publishOrderEvent(cancelledOrder, "ORDER_CANCELLED");
        
        log.info("Order {} cancelled", order.getOrderNumber());
        return mapToResponse(cancelledOrder);
    }
    
    private void publishOrderEvent(Order order, String eventType) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .eventType(eventType)
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            rabbitTemplate.convertAndSend(orderExchange, orderCreatedRoutingKey, event);
            log.info("Published {} event for order {}", eventType, order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish order event", e);
        }
    }
    
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(itemResponses)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
