package com.healthcare.ordermanagement.manager;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.domain.OrderType;
import com.healthcare.ordermanagement.domain.Priority;
import com.healthcare.ordermanagement.engine.TriagingEngine;
import com.healthcare.ordermanagement.pattern.command.CommandLog;
import com.healthcare.ordermanagement.pattern.decorator.OrderHandlerFactory;
import com.healthcare.ordermanagement.pattern.decorator.BaseOrderHandler;
import com.healthcare.ordermanagement.pattern.factory.OrderFactory;
import com.healthcare.ordermanagement.pattern.observer.NotificationService;
import com.healthcare.ordermanagement.pattern.strategy.PriorityTriageStrategy;
import com.healthcare.ordermanagement.resource.InMemoryOrderAccess;
import com.healthcare.ordermanagement.resource.OrderAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderManagerTest {

    // Real implementations for state-bearing components
    private OrderAccess orderAccess;
    private TriagingEngine triagingEngine;
    private CommandLog commandLog;
    private OrderFactory orderFactory;
    private OrderHandlerFactory orderHandlerFactory;

    // Mocked — we don't want real console output in tests
    @Mock
    private NotificationService notificationService;

    private OrderManager orderManager;

    @BeforeEach
    void setUp() {
        orderAccess      = new InMemoryOrderAccess();
        triagingEngine   = new TriagingEngine(new PriorityTriageStrategy());
        commandLog       = new CommandLog();
        orderFactory     = new OrderFactory();
        orderHandlerFactory = new OrderHandlerFactory(new BaseOrderHandler());

        orderManager = new OrderManager(
            orderFactory,
            orderAccess,
            triagingEngine,
            notificationService,
            commandLog,
            orderHandlerFactory
        );
    }

    // ── Submit order ───────────────────────────────────────────────────────────

    @Test
    void submitOrder_returnsOrderWithPendingStatus() {
        Order order = orderManager.submitOrder(
            "LAB", "John Smith", "Dr. Adams", "Blood test", "ROUTINE"
        );
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void submitOrder_firesNotification() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        verify(notificationService, times(1)).notify(any(Order.class), eq("ORDER_SUBMITTED"));
    }

    @Test
    void submitOrder_appearsInQueue() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        assertEquals(1, orderManager.getQueue().size());
    }

    @Test
    void submitOrder_recordedInAuditLog() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        assertEquals(1, orderManager.getCommandLog().size());
        assertEquals("SUBMIT", orderManager.getCommandLog().get(0).getCommandType());
    }

    // ── Triage ordering ────────────────────────────────────────────────────────

    @Test
    void statOrderBeforeRoutine_inQueue() {
        orderManager.submitOrder("LAB", "Patient A", "Dr. A", "Test", "ROUTINE");
        orderManager.submitOrder("LAB", "Patient B", "Dr. B", "Test", "STAT");

        assertEquals(Priority.STAT, orderManager.getQueue().get(0).getPriority());
        assertEquals(Priority.ROUTINE, orderManager.getQueue().get(1).getPriority());
    }

    @Test
    void statOrderBeforeUrgentBeforeRoutine() {
        orderManager.submitOrder("LAB", "Patient A", "Dr. A", "Test", "ROUTINE");
        orderManager.submitOrder("LAB", "Patient B", "Dr. B", "Test", "URGENT");
        orderManager.submitOrder("LAB", "Patient C", "Dr. C", "Test", "STAT");

        assertEquals(Priority.STAT,    orderManager.getQueue().get(0).getPriority());
        assertEquals(Priority.URGENT,  orderManager.getQueue().get(1).getPriority());
        assertEquals(Priority.ROUTINE, orderManager.getQueue().get(2).getPriority());
    }

    // ── Claim order ────────────────────────────────────────────────────────────

    @Test
    void claimNextOrder_statusChangesToInProgress() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order claimed = orderManager.claimNextOrder("Nurse Kim");
        assertEquals(OrderStatus.IN_PROGRESS, claimed.getStatus());
    }

    @Test
    void claimNextOrder_lockedToStaffMember() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order claimed = orderManager.claimNextOrder("Nurse Kim");
        assertEquals("Nurse Kim", claimed.getClaimedBy());
    }

    @Test
    void claimNextOrder_removedFromQueue() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        orderManager.claimNextOrder("Nurse Kim");
        assertEquals(0, orderManager.getQueue().size());
    }

    @Test
    void claimNextOrder_firesNotification() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        orderManager.claimNextOrder("Nurse Kim");
        verify(notificationService, times(1)).notify(any(Order.class), eq("ORDER_CLAIMED"));
    }

    @Test
    void claimNextOrder_emptyQueue_throwsException() {
        assertThrows(IllegalStateException.class, () ->
            orderManager.claimNextOrder("Nurse Kim")
        );
    }

    // ── Complete order ─────────────────────────────────────────────────────────

    @Test
    void completeOrder_statusChangesToCompleted() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order claimed = orderManager.claimNextOrder("Nurse Kim");
        Order completed = orderManager.completeOrder(claimed.getOrderId(), "Nurse Kim");
        assertEquals(OrderStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void completeOrder_wrongStaff_throwsException() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order claimed = orderManager.claimNextOrder("Nurse Kim");
        assertThrows(IllegalStateException.class, () ->
            orderManager.completeOrder(claimed.getOrderId(), "Nurse Johnson")
        );
    }

    @Test
    void completeOrder_firesNotification() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order claimed = orderManager.claimNextOrder("Nurse Kim");
        orderManager.completeOrder(claimed.getOrderId(), "Nurse Kim");
        verify(notificationService, times(1)).notify(any(Order.class), eq("ORDER_COMPLETED"));
    }

    // ── Cancel order ───────────────────────────────────────────────────────────

    @Test
    void cancelOrder_statusChangesToCancelled() {
        Order order = orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order cancelled = orderManager.cancelOrder(order.getOrderId(), "Dr. A");
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void cancelOrder_removedFromQueue() {
        Order order = orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        orderManager.cancelOrder(order.getOrderId(), "Dr. A");
        assertEquals(0, orderManager.getQueue().size());
    }

    @Test
    void cancelInProgressOrder_throwsException() {
        orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order claimed = orderManager.claimNextOrder("Nurse Kim");
        assertThrows(IllegalStateException.class, () ->
            orderManager.cancelOrder(claimed.getOrderId(), "Dr. A")
        );
    }

    @Test
    void cancelOrder_wrongClinician_throwsException() {
        Order order = orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        assertThrows(IllegalStateException.class, () ->
            orderManager.cancelOrder(order.getOrderId(), "Dr. Wrong")
        );
    }

    @Test
    void cancelOrder_firesNotification() {
        Order order = orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        orderManager.cancelOrder(order.getOrderId(), "Dr. A");
        verify(notificationService, times(1)).notify(any(Order.class), eq("ORDER_CANCELLED"));
    }

    // ── Audit log ──────────────────────────────────────────────────────────────

    @Test
    void fullWorkflow_auditLogHasAllEntries() {
        Order order = orderManager.submitOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order claimed = orderManager.claimNextOrder("Nurse Kim");
        orderManager.completeOrder(claimed.getOrderId(), "Nurse Kim");

        assertEquals(3, orderManager.getCommandLog().size());
        assertEquals("SUBMIT",   orderManager.getCommandLog().get(0).getCommandType());
        assertEquals("CLAIM",    orderManager.getCommandLog().get(1).getCommandType());
        assertEquals("COMPLETE", orderManager.getCommandLog().get(2).getCommandType());
    }
}