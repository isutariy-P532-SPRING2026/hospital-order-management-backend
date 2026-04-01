package com.healthcare.ordermanagement.pattern.decorator;

import com.healthcare.ordermanagement.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class BaseOrderHandler implements OrderHandler {

    @Override
    public void handle(Order order) {
        System.out.printf(
            "[BASE HANDLER] Processing order %s for patient %s%n",
            order.getOrderId(),
            order.getPatientName()
        );
    }
}