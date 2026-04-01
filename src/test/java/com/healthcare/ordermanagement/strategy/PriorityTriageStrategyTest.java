package com.healthcare.ordermanagement.strategy;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderType;
import com.healthcare.ordermanagement.domain.Priority;
import com.healthcare.ordermanagement.pattern.strategy.PriorityTriageStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriorityTriageStrategyTest {

    private PriorityTriageStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PriorityTriageStrategy();
    }

    // ── Empty queue ────────────────────────────────────────────────────────────

    @Test
    void emptyQueue_newOrderGoesToPositionZero() {
        Order order = makeOrder(Priority.ROUTINE);
        int pos = strategy.determinePosition(new ArrayList<>(), order);
        assertEquals(0, pos);
    }

    // ── STAT always goes to front ──────────────────────────────────────────────

    @Test
    void statOrder_goesBeforeUrgentAndRoutine() {
        List<Order> queue = new ArrayList<>();
        queue.add(makeOrder(Priority.URGENT));
        queue.add(makeOrder(Priority.ROUTINE));

        Order stat = makeOrder(Priority.STAT);
        int pos = strategy.determinePosition(queue, stat);
        assertEquals(0, pos);
    }

    @Test
    void statOrder_goesBeforeAnotherStat_fifo() {
        List<Order> queue = new ArrayList<>();
        queue.add(makeOrder(Priority.STAT));

        Order newStat = makeOrder(Priority.STAT);
        int pos = strategy.determinePosition(queue, newStat);
        // FIFO: new STAT goes AFTER existing STAT
        assertEquals(1, pos);
    }

    // ── URGENT ordering ────────────────────────────────────────────────────────

    @Test
    void urgentOrder_goesAfterStatBeforeRoutine() {
        List<Order> queue = new ArrayList<>();
        queue.add(makeOrder(Priority.STAT));
        queue.add(makeOrder(Priority.ROUTINE));

        Order urgent = makeOrder(Priority.URGENT);
        int pos = strategy.determinePosition(queue, urgent);
        assertEquals(1, pos);
    }

    @Test
    void urgentOrder_goesAfterExistingUrgent_fifo() {
        List<Order> queue = new ArrayList<>();
        queue.add(makeOrder(Priority.STAT));
        queue.add(makeOrder(Priority.URGENT));
        queue.add(makeOrder(Priority.ROUTINE));

        Order urgent = makeOrder(Priority.URGENT);
        int pos = strategy.determinePosition(queue, urgent);
        // Goes after existing URGENT, before ROUTINE
        assertEquals(2, pos);
    }

    // ── ROUTINE always goes to end ─────────────────────────────────────────────

    @Test
    void routineOrder_goesToEnd() {
        List<Order> queue = new ArrayList<>();
        queue.add(makeOrder(Priority.STAT));
        queue.add(makeOrder(Priority.URGENT));
        queue.add(makeOrder(Priority.ROUTINE));

        Order routine = makeOrder(Priority.ROUTINE);
        int pos = strategy.determinePosition(queue, routine);
        assertEquals(3, pos);
    }

    @Test
    void routineOrder_emptyQueue_goesToPositionZero() {
        Order routine = makeOrder(Priority.ROUTINE);
        int pos = strategy.determinePosition(new ArrayList<>(), routine);
        assertEquals(0, pos);
    }

    // ── Mixed scenario ─────────────────────────────────────────────────────────

    @Test
    void mixedQueue_correctOrdering() {
        List<Order> queue = new ArrayList<>();
        queue.add(makeOrder(Priority.STAT));
        queue.add(makeOrder(Priority.URGENT));
        queue.add(makeOrder(Priority.URGENT));
        queue.add(makeOrder(Priority.ROUTINE));

        // New URGENT should go after both existing URGENTs (index 3)
        Order urgent = makeOrder(Priority.URGENT);
        int pos = strategy.determinePosition(queue, urgent);
        assertEquals(3, pos);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private Order makeOrder(Priority priority) {
        return new Order(
            "TEST-001",
            OrderType.LAB,
            "Patient A",
            "Dr. Test",
            "Test description",
            priority
        );
    }
}