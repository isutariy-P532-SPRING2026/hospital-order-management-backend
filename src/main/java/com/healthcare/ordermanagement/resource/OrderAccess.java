package com.healthcare.ordermanagement.resource;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import java.util.List;

public interface OrderAccess {

    void saveOrder(Order order);

    Order findOrderById(String orderId);

    void deleteOrder(String orderId);       

    List<Order> listAllOrders();

    List<Order> listOrdersByStatus(OrderStatus status);
}