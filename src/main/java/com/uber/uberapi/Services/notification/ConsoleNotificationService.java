package com.uber.uberapi.Services.notification;

import org.springframework.stereotype.Service;

@Service
public class ConsoleNotificationService implements NotificationService {
    @Override
    public void notify(String phoneNumber, String message) {
        System.out.printf("Notification for %s: %s",phoneNumber,message);

    }
}
