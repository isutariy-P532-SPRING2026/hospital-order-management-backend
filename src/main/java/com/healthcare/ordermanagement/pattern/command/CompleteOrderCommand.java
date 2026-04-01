package com.healthcare.ordermanagement.pattern.command;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.pattern.observer.NotificationService;
import com.healthcare.ordermanagement.resource.OrderAccess;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CompleteOrderCommand implements Command {

    private final String orderId;
    private final String actor;
    private final OrderAccess orderAccess;
    private final NotificationService notificationService;
    private final LocalDateTime executedAt = LocalDateTime.now();

    public CompleteOrderCommand(String orderId,
                                String actor,
                                OrderAccess orderAccess,
                                NotificationService notificationService) {
        this.orderId             = orderId;
        this.actor               = actor;
        this.orderAccess         = orderAccess;
        this.notificationService = notificationService;
    }

    @Override
    public void execute() {
        Order order = orderAccess.findOrderById(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        orderAccess.saveOrder(order);
        notificationService.notify(order, "ORDER_COMPLETED");
    }

    @Override
    public void undo() {
        
        Order order = orderAccess.findOrderById(orderId);
        order.setStatus(OrderStatus.IN_PROGRESS);
        orderAccess.saveOrder(order);
        notificationService.notify(order, "ORDER_COMPLETION_UNDONE");
    }

    @Override public String getCommandType() { return "COMPLETE"; }
    @Override public String getActor()       { return actor; }
}