package com.healthcare.ordermanagement.resource;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryOrderAccess implements OrderAccess {

    /**
     * LinkedHashMap preserves insertion order.
     * Key = orderId, Value = Order.
     * (All access goes through this class — no other class touches this map.)
     */
    private final Map<String, Order> store = new LinkedHashMap<>();

    @Override
    public void saveOrder(Order order) {
        store.put(order.getOrderId(), order);
    }

    @Override
    public Order findOrderById(String orderId) {
        Order order = store.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }

    @Override
    public void deleteOrder(String orderId) {
        if (!store.containsKey(orderId)) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        store.remove(orderId);
    }

    @Override
    public List<Order> listAllOrders() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    @Override
    public List<Order> listOrdersByStatus(OrderStatus status) {
        return store.values().stream()
                .filter(o -> o.getStatus() == status)
                .toList();
    }
}