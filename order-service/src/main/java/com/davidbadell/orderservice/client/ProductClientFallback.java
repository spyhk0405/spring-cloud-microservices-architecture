package com.davidbadell.orderservice.client;

import com.davidbadell.orderservice.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {
    
    @Override
    public ProductDTO getProductById(Long id) {
        log.warn("Fallback: Product service unavailable for product id: {}", id);
        return ProductDTO.builder()
                .id(id)
                .name("Service Unavailable")
                .price(BigDecimal.ZERO)
                .stockQuantity(0)
                .active(false)
                .build();
    }
    
    @Override
    public Boolean reduceStock(Long id, Integer quantity) {
        log.warn("Fallback: Unable to reduce stock for product id: {}", id);
        return false;
    }
}
