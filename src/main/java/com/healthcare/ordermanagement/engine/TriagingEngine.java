package com.healthcare.ordermanagement.engine;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.pattern.strategy.TriageStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TriagingEngine {

    private final TriageStrategy triageStrategy;

    // Spring injects whichever TriageStrategy bean is active.
    
    public TriagingEngine(TriageStrategy triageStrategy) {
        this.triageStrategy = triageStrategy;
    }


    private final List<Order> queue = new ArrayList<>();

    // ── public API ────────────────────────────────────────────────────────────

    // Insert a new order into the correct triage position.
    public void enqueue(Order order) {
        int position = triageStrategy.determinePosition(queue, order);
        queue.add(position, order);
    }

    // Remove an order from the queue (when claimed, cancelled, or undone).
    public void dequeue(String orderId) {
        queue.removeIf(o -> o.getOrderId().equals(orderId));
    }

    // Returns the sorted queue of PENDING orders only — what the UI displays.
    public List<Order> getSortedQueue() {
        return Collections.unmodifiableList(
            queue.stream()
                 .filter(o -> o.getStatus() == OrderStatus.PENDING)
                 .toList()
        );
    }

    // Peek at the next order to be fulfilled without removing it.
    public Order peekNext() {
        return queue.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Queue is empty"));
    }

    public void requeue(Order order) {
        dequeue(order.getOrderId());
        enqueue(order);
    }
}