package com.davidbadell.orderservice.client;

import com.davidbadell.orderservice.dto.ProductDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "PRODUCT-SERVICE", fallback = ProductClientFallback.class)
public interface ProductClient {
    
    @GetMapping("/api/products/{id}")
    @CircuitBreaker(name = "productService")
    ProductDTO getProductById(@PathVariable("id") Long id);
    
    @PostMapping("/api/products/{id}/reduce-stock")
    @CircuitBreaker(name = "productService")
    Boolean reduceStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}
