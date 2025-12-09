package com.davidbadell.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String eventType;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime timestamp;
}
