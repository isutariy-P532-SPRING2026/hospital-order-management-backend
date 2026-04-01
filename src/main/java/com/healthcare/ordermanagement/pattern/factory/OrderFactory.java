package com.healthcare.ordermanagement.pattern.factory;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderType;
import com.healthcare.ordermanagement.domain.Priority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderFactory {

    /**
     * Single creation point for all order types.
     * Callers never use "new Order(...)" directly — they always go through here.
     */
    public Order createOrder(String orderTypeStr,
                             String patientName,
                             String clinicianName,
                             String description,
                             String priorityStr) {

        OrderType type = parseOrderType(orderTypeStr);
        Priority priority = parsePriority(priorityStr);
        String orderId = generateOrderId(type);

        return switch (type) {
            case LAB        -> createLabOrder(orderId, patientName, clinicianName, description, priority);
            case MEDICATION -> createMedicationOrder(orderId, patientName, clinicianName, description, priority);
            case IMAGING    -> createImagingOrder(orderId, patientName, clinicianName, description, priority);
        };
    }

    // ── private creation methods (one per type) ───────────────────────────────

    private Order createLabOrder(String orderId, String patientName,
                                 String clinicianName, String description,
                                 Priority priority) {
        Order order = new Order(orderId, OrderType.LAB, patientName,
                                clinicianName, description, priority);
        return order;
    }

    private Order createMedicationOrder(String orderId, String patientName,
                                        String clinicianName, String description,
                                        Priority priority) {
        Order order = new Order(orderId, OrderType.MEDICATION, patientName,
                                clinicianName, description, priority);
        return order;
    }

    private Order createImagingOrder(String orderId, String patientName,
                                     String clinicianName, String description,
                                     Priority priority) {
        Order order = new Order(orderId, OrderType.IMAGING, patientName,
                                clinicianName, description, priority);
        return order;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String generateOrderId(OrderType type) {
        String prefix = switch (type) {
            case LAB        -> "LAB";
            case MEDICATION -> "MED";
            case IMAGING    -> "IMG";
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderType parseOrderType(String value) {
        try {
            return OrderType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown order type: " + value);
        }
    }

    private Priority parsePriority(String value) {
        try {
            return Priority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown priority: " + value);
        }
    }
}