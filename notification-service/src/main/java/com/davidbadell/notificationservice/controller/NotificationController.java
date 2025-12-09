package com.davidbadell.notificationservice.controller;

import com.davidbadell.notificationservice.dto.NotificationRequest;
import com.davidbadell.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final EmailService emailService;
    
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody NotificationRequest request) {
        emailService.sendEmail(request);
        return ResponseEntity.ok("Email sent successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
