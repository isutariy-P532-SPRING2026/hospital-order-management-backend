package com.healthcare.ordermanagement.pattern.decorator;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.Priority;

import java.time.LocalDateTime;

public class PriorityBoostingDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    // Orders waiting longer than this many minutes get boosted to STAT.
   
    private static final long BOOST_THRESHOLD_MINUTES = 30;

    public PriorityBoostingDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    // Boosts URGENT orders that have been waiting too long to STAT.
    // Runs after validation, before audit logging.
    
    @Override
    public void handle(Order order) {
        boost(order);
        wrapped.handle(order);
    }

    private void boost(Order order) {
        if (order.getPriority() == Priority.URGENT) {
            long waitMinutes = java.time.Duration.between(
                order.getSubmittedAt(),
                LocalDateTime.now()
            ).toMinutes();

            if (waitMinutes >= BOOST_THRESHOLD_MINUTES) {
                order.setPriority(Priority.STAT);
                System.out.printf(
                    "[PRIORITY BOOST] Order %s boosted to STAT after %d minutes%n",
                    order.getOrderId(), waitMinutes
                );
            }
        }
    }
}