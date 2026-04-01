package com.healthcare.ordermanagement.pattern.command;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.pattern.observer.NotificationService;
import com.healthcare.ordermanagement.resource.OrderAccess;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CancelOrderCommand implements Command {

    private final String orderId;
    private final String actor;
    private final OrderAccess orderAccess;
    private final NotificationService notificationService;
    private final LocalDateTime executedAt = LocalDateTime.now();

    public CancelOrderCommand(String orderId,
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

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot cancel order " + orderId + " — status is " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderAccess.saveOrder(order);
        notificationService.notify(order, "ORDER_CANCELLED");
    }

    @Override
    public void undo() {
        
        Order order = orderAccess.findOrderById(orderId);
        order.setStatus(OrderStatus.PENDING);
        orderAccess.saveOrder(order);
        notificationService.notify(order, "ORDER_CANCELLATION_UNDONE");
    }

    @Override public String getCommandType() { return "CANCEL"; }
    @Override public String getActor()       { return actor; }
}