package com.davidbadell.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(nullable = false)
    private Long productId;
    
    private String productName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
