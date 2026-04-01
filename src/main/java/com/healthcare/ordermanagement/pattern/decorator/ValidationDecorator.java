package com.healthcare.ordermanagement.pattern.decorator;

import com.healthcare.ordermanagement.domain.Order;

public class ValidationDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    public ValidationDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    // Runs first in the chain — rejects bad orders before anything else touches them.

    @Override
    public void handle(Order order) {
        validate(order);
        wrapped.handle(order);
    }

    private void validate(Order order) {
        if (order.getPatientName() == null || order.getPatientName().isBlank()) {
            throw new IllegalArgumentException("Patient name is required");
        }
        if (order.getClinicianName() == null || order.getClinicianName().isBlank()) {
            throw new IllegalArgumentException("Clinician name is required");
        }
        if (order.getDescription() == null || order.getDescription().isBlank()) {
            throw new IllegalArgumentException("Order description is required");
        }
        if (order.getPriority() == null) {
            throw new IllegalArgumentException("Priority is required");
        }
        if (order.getType() == null) {
            throw new IllegalArgumentException("Order type is required");
        }
        System.out.printf("[VALIDATION] Order %s passed validation%n",
            order.getOrderId());
    }
}