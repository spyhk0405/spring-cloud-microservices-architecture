package com.davidbadell.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String to;
    private String subject;
    private String body;
    private NotificationType type;
    
    public enum NotificationType {
        EMAIL,
        SMS,
        PUSH
    }
}
