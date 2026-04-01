package com.healthcare.ordermanagement.pattern.observer;

import com.healthcare.ordermanagement.domain.Order;

public interface NotificationService {

    /**
     * Notify relevant parties that something happened to an order.
     *
     * @param order  the order that changed
     * @param event  what happened (e.g. "ORDER_SUBMITTED", "ORDER_CLAIMED")
     */
    void notify(Order order, String event);
}