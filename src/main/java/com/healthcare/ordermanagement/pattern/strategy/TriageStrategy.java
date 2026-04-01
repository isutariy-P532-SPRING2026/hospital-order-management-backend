package com.healthcare.ordermanagement.pattern.strategy;

import com.healthcare.ordermanagement.domain.Order;
import java.util.List;

public interface TriageStrategy {
    
    // Takes the current queue and a new order,
    // returns the position (index) where the new order should be inserted.
    
    int determinePosition(List<Order> currentQueue, Order newOrder);
}