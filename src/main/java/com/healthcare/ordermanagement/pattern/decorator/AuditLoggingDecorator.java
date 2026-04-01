package com.healthcare.ordermanagement.pattern.decorator;

import com.healthcare.ordermanagement.domain.Order;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLoggingDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLoggingDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    // Logs before and after the handler below it runs.
     
    @Override
    public void handle(Order order) {
        System.out.printf(
            "[AUDIT] %s | START processing order %s | Type: %s | Priority: %s%n",
            FMT.format(LocalDateTime.now()),
            order.getOrderId(),
            order.getType(),
            order.getPriority()
        );

        wrapped.handle(order);

        System.out.printf(
            "[AUDIT] %s | END   processing order %s | Status: %s%n",
            FMT.format(LocalDateTime.now()),
            order.getOrderId(),
            order.getStatus()
        );
    }
}