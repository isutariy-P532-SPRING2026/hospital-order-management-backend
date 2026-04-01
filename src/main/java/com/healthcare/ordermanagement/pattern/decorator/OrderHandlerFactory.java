package com.healthcare.ordermanagement.pattern.decorator;

import com.healthcare.ordermanagement.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderHandlerFactory {

    private final BaseOrderHandler baseOrderHandler;

    public OrderHandlerFactory(BaseOrderHandler baseOrderHandler) {
        this.baseOrderHandler = baseOrderHandler;
    }

    
    // Builds the decorator chain:
    // ValidationDecorator → PriorityBoostingDecorator → AuditLoggingDecorator → BaseOrderHandler
     
    public OrderHandler buildChain() {
        OrderHandler handler = baseOrderHandler;
        handler = new AuditLoggingDecorator(handler);
        handler = new PriorityBoostingDecorator(handler);
        handler = new ValidationDecorator(handler);
        return handler;
    }
}