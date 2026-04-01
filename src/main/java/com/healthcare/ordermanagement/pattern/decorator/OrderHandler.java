package com.healthcare.ordermanagement.pattern.decorator;

import com.healthcare.ordermanagement.domain.Order;

public interface OrderHandler {
    
    void handle(Order order);
}