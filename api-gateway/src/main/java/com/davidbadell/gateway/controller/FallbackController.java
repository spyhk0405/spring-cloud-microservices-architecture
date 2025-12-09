package com.davidbadell.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> usersFallback() {
        return createFallbackResponse("User Service");
    }
    
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> productsFallback() {
        return createFallbackResponse("Product Service");
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> ordersFallback() {
        return createFallbackResponse("Order Service");
    }
    
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", serviceName + " is currently unavailable. Please try again later.");
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
