package com.healthcare.ordermanagement.factory;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.domain.OrderType;
import com.healthcare.ordermanagement.domain.Priority;
import com.healthcare.ordermanagement.pattern.factory.OrderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderFactoryTest {

    private OrderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new OrderFactory();
    }

    // ── Correct types created ──────────────────────────────────────────────────

    @Test
    void createLabOrder_hasCorrectType() {
        Order order = factory.createOrder("LAB", "John", "Dr. A", "Blood test", "ROUTINE");
        assertEquals(OrderType.LAB, order.getType());
    }

    @Test
    void createMedicationOrder_hasCorrectType() {
        Order order = factory.createOrder("MEDICATION", "Jane", "Dr. B", "Aspirin", "URGENT");
        assertEquals(OrderType.MEDICATION, order.getType());
    }

    @Test
    void createImagingOrder_hasCorrectType() {
        Order order = factory.createOrder("IMAGING", "Bob", "Dr. C", "Chest X-ray", "STAT");
        assertEquals(OrderType.IMAGING, order.getType());
    }

    // ── ID prefixes ────────────────────────────────────────────────────────────

    @Test
    void labOrder_idStartsWithLAB() {
        Order order = factory.createOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        assertTrue(order.getOrderId().startsWith("LAB-"));
    }

    @Test
    void medicationOrder_idStartsWithMED() {
        Order order = factory.createOrder("MEDICATION", "Jane", "Dr. B", "Test", "URGENT");
        assertTrue(order.getOrderId().startsWith("MED-"));
    }

    @Test
    void imagingOrder_idStartsWithIMG() {
        Order order = factory.createOrder("IMAGING", "Bob", "Dr. C", "Test", "STAT");
        assertTrue(order.getOrderId().startsWith("IMG-"));
    }

    // ── Priority and status ────────────────────────────────────────────────────

    @Test
    void createOrder_correctPriority() {
        Order order = factory.createOrder("LAB", "John", "Dr. A", "Test", "STAT");
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void newOrder_statusIsPending() {
        Order order = factory.createOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void newOrder_claimedByIsNull() {
        Order order = factory.createOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        assertNull(order.getClaimedBy());
    }

    // ── Field mapping ──────────────────────────────────────────────────────────

    @Test
    void createOrder_fieldsMapCorrectly() {
        Order order = factory.createOrder(
            "LAB", "Alice Smith", "Dr. Jones", "CBC panel", "URGENT"
        );
        assertEquals("Alice Smith", order.getPatientName());
        assertEquals("Dr. Jones", order.getClinicianName());
        assertEquals("CBC panel", order.getDescription());
    }

    @Test
    void createOrder_caseInsensitiveType() {
        Order order = factory.createOrder("lab", "John", "Dr. A", "Test", "ROUTINE");
        assertEquals(OrderType.LAB, order.getType());
    }

    @Test
    void createOrder_caseInsensitivePriority() {
        Order order = factory.createOrder("LAB", "John", "Dr. A", "Test", "stat");
        assertEquals(Priority.STAT, order.getPriority());
    }

    // ── Error handling ─────────────────────────────────────────────────────────

    @Test
    void unknownOrderType_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            factory.createOrder("SURGERY", "John", "Dr. A", "Test", "ROUTINE")
        );
    }

    @Test
    void unknownPriority_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            factory.createOrder("LAB", "John", "Dr. A", "Test", "CRITICAL")
        );
    }

    // ── Unique IDs ─────────────────────────────────────────────────────────────

    @Test
    void twoOrders_haveUniqueIds() {
        Order o1 = factory.createOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        Order o2 = factory.createOrder("LAB", "John", "Dr. A", "Test", "ROUTINE");
        assertNotEquals(o1.getOrderId(), o2.getOrderId());
    }
}