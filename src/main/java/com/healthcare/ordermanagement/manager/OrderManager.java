package com.healthcare.ordermanagement.manager;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.engine.TriagingEngine;
import com.healthcare.ordermanagement.pattern.command.*;
import com.healthcare.ordermanagement.pattern.decorator.OrderHandler;
import com.healthcare.ordermanagement.pattern.decorator.OrderHandlerFactory;
import com.healthcare.ordermanagement.pattern.factory.OrderFactory;
import com.healthcare.ordermanagement.pattern.observer.NotificationService;
import com.healthcare.ordermanagement.resource.OrderAccess;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderManager {

    private final OrderFactory orderFactory;
    private final OrderAccess orderAccess;
    private final TriagingEngine triagingEngine;
    private final NotificationService notificationService;
    private final CommandLog commandLog;
    private final OrderHandlerFactory orderHandlerFactory;

    
    public OrderManager(OrderFactory orderFactory,
                        OrderAccess orderAccess,
                        TriagingEngine triagingEngine,
                        NotificationService notificationService,
                        CommandLog commandLog,
                        OrderHandlerFactory orderHandlerFactory) {
        this.orderFactory        = orderFactory;
        this.orderAccess         = orderAccess;
        this.triagingEngine      = triagingEngine;
        this.notificationService = notificationService;
        this.commandLog          = commandLog;
        this.orderHandlerFactory = orderHandlerFactory;
    }

    // ── Use Case 1: Submit an order ───────────────────────────────────────────

    /**
     * Handles a new order submission from a clinician.
     * The flow is:
     * - Create the right type of order
     * - Run it through validation and processing steps
     * - Place it into the triage queue
     * - Save it and send a notification
     * - Log the action for auditing
     */
    public Order submitOrder(String orderType,
                             String patientName,
                             String clinicianName,
                             String description,
                             String priority) {

        // Create the correct order type (Lab, Medication, Imaging)
        Order order = orderFactory.createOrder(
            orderType, patientName, clinicianName, description, priority
        );

        // Run the order through the decorator chain (validation, logging, etc.)
        OrderHandler chain = orderHandlerFactory.buildChain();
        chain.handle(order);

        // Add the order to the triage queue based on priority
        triagingEngine.enqueue(order);

        // Execute the command to save the order and notify the system
        Command command = new SubmitOrderCommand(order, orderAccess, notificationService);
        command.execute();

        // Store the command in the audit log
        commandLog.record(command);

        return order;
    }

    // ── Use Case 2a: Claim an order ───────────────────────────────────────────

    /**
     * Allows a staff member to claim the next available order.
     * Once claimed, the order is locked so others can’t take it.
     */
    public Order claimNextOrder(String staffName) {
        Order next = triagingEngine.peekNext();

        // Make sure the order isn’t already claimed
        if (next.getClaimedBy() != null) {
            throw new IllegalStateException(
                "Order " + next.getOrderId() + " is already claimed by " + next.getClaimedBy()
            );
        }

        // Remove it from the queue since it's now being worked on
        triagingEngine.dequeue(next.getOrderId());

        Command command = new ClaimOrderCommand(
            next.getOrderId(), staffName, orderAccess, notificationService
        );
        command.execute();
        commandLog.record(command);

        return orderAccess.findOrderById(next.getOrderId());
    }

    // ── Use Case 2b: Complete an order ────────────────────────────────────────

    /**
     * Marks an order as completed by the staff member who claimed it.
     */
    public Order completeOrder(String orderId, String staffName) {
        Order order = orderAccess.findOrderById(orderId);

        // Only the person who claimed the order can complete it
        if (!staffName.equals(order.getClaimedBy())) {
            throw new IllegalStateException(
                staffName + " did not claim order " + orderId
            );
        }

        // Order must be in progress before it can be completed
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Order " + orderId + " is not in progress — status is " + order.getStatus()
            );
        }

        Command command = new CompleteOrderCommand(orderId, staffName, orderAccess, notificationService);
        command.execute();
        commandLog.record(command);

        return orderAccess.findOrderById(orderId);
    }

    // ── Use Case 3: Cancel an order ───────────────────────────────────────────

    /**
     * Allows the clinician who created the order to cancel it,
     * as long as it hasn’t started processing yet.
     */
    public Order cancelOrder(String orderId, String clinicianName) {
        Order order = orderAccess.findOrderById(orderId);

        // Only the original clinician can cancel the order
        if (!clinicianName.equals(order.getClinicianName())) {
            throw new IllegalStateException(
                clinicianName + " did not submit order " + orderId
            );
        }

        // Remove from queue before cancelling
        triagingEngine.dequeue(orderId);

        Command command = new CancelOrderCommand(orderId, clinicianName, orderAccess, notificationService);
        command.execute();
        commandLog.record(command);

        return orderAccess.findOrderById(orderId);
    }

  
    public boolean undoLastCommand() {
        return commandLog.undoLast();
    }

    
    public List<Order> getQueue() {
        return triagingEngine.getSortedQueue();
    }

    public List<Order> getAllOrders() {
        return orderAccess.listAllOrders();
    }

    public Order getOrderById(String orderId) {
        return orderAccess.findOrderById(orderId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderAccess.listOrdersByStatus(status);
    }

    public List<CommandLogEntry> getCommandLog() {
        return commandLog.getAll();
    }
}