package com.healthcare.ordermanagement.pattern.observer;

import com.healthcare.ordermanagement.domain.Order;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class ConsoleNotificationService implements NotificationService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    
    @Override
    public void notify(Order order, String event) {
        System.out.printf(
            "[NOTIFICATION] %s | Event: %-30s | Order: %s | Type: %-10s | " +
            "Patient: %-20s | Clinician: %-15s | Priority: %-8s | Status: %s%n",
            FMT.format(java.time.LocalDateTime.now()),
            event,
            order.getOrderId(),
            order.getType(),
            order.getPatientName(),
            order.getClinicianName(),
            order.getPriority(),
            order.getStatus()
        );
    }
}