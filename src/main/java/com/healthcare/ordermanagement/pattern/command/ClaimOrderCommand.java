package com.healthcare.ordermanagement.pattern.command;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.pattern.observer.NotificationService;
import com.healthcare.ordermanagement.resource.OrderAccess;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ClaimOrderCommand implements Command {

    private final String orderId;
    private final String actor;
    private final OrderAccess orderAccess;
    private final NotificationService notificationService;
    private final LocalDateTime executedAt = LocalDateTime.now();

    public ClaimOrderCommand(String orderId,
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
        order.setClaimedBy(actor);
        order.setStatus(OrderStatus.IN_PROGRESS);
        orderAccess.saveOrder(order);
        notificationService.notify(order, "ORDER_CLAIMED");
    }

    @Override
    public void undo() {
        
        Order order = orderAccess.findOrderById(orderId);
        order.setClaimedBy(null);
        order.setStatus(OrderStatus.PENDING);
        orderAccess.saveOrder(order);
        notificationService.notify(order, "ORDER_CLAIM_UNDONE");
    }

    @Override public String getCommandType() { return "CLAIM"; }
    @Override public String getActor()       { return actor; }
}