package com.healthcare.ordermanagement.pattern.strategy;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.Priority;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PriorityTriageStrategy implements TriageStrategy {


    @Override
    public int determinePosition(List<Order> currentQueue, Order newOrder) {
        int newScore = priorityScore(newOrder.getPriority());

        for (int i = 0; i < currentQueue.size(); i++) {
            int existingScore = priorityScore(currentQueue.get(i).getPriority());

            if (newScore > existingScore) {
                return i;
            }

            if (newScore == existingScore) {
                // Same priority — FIFO: new order goes after all existing ones of same priority
                continue;
            }
        }

        // Lower priority than everything, or queue is empty → go to end
        return currentQueue.size();
    }

    private int priorityScore(Priority priority) {
        return switch (priority) {
            case STAT    -> 3;
            case URGENT  -> 2;
            case ROUTINE -> 1;
        };
    }
}