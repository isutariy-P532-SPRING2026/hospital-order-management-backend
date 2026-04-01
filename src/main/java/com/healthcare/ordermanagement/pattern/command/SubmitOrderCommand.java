package com.healthcare.ordermanagement.pattern.command;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.pattern.observer.NotificationService;
import com.healthcare.ordermanagement.resource.OrderAccess;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class SubmitOrderCommand implements Command {

    private final Order order;
    private final OrderAccess orderAccess;
    private final NotificationService notificationService;
    private final LocalDateTime executedAt = LocalDateTime.now();

    public SubmitOrderCommand(Order order,
                              OrderAccess orderAccess,
                              NotificationService notificationService) {
        this.order               = order;
        this.orderAccess         = orderAccess;
        this.notificationService = notificationService;
    }

    @Override
    public void execute() {
        orderAccess.saveOrder(order);
        notificationService.notify(order, "ORDER_SUBMITTED");
    }

    @Override
    public void undo() {
        
        orderAccess.deleteOrder(order.getOrderId());
        notificationService.notify(order, "ORDER_SUBMISSION_UNDONE");
    }

    @Override public String getCommandType() { return "SUBMIT"; }
    @Override public String getOrderId()     { return order.getOrderId(); }
    @Override public String getActor()       { return order.getClinicianName(); }
}