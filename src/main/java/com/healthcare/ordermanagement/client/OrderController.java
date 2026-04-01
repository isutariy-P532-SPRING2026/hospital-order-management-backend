package com.healthcare.ordermanagement.client;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.domain.OrderStatus;
import com.healthcare.ordermanagement.manager.OrderManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")   
public class OrderController {

    private final OrderManager orderManager;

    public OrderController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    // ── Submit a new order ────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> submitOrder(@RequestBody Map<String, String> body) {
        try {
            Order order = orderManager.submitOrder(
                body.get("orderType"),
                body.get("patientName"),
                body.get("clinicianName"),
                body.get("description"),
                body.get("priority")
            );
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Get the sorted triage queue (PENDING orders only) ────────────────────

    @GetMapping("/queue")
    public ResponseEntity<List<Order>> getQueue() {
        return ResponseEntity.ok(orderManager.getQueue());
    }

    // ── Get all orders (all statuses) ─────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderManager.getAllOrders());
    }

    // ── Get a single order by ID ──────────────────────────────────────────────

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(orderManager.getOrderById(orderId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Get orders filtered by status ─────────────────────────────────────────

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(orderManager.getOrdersByStatus(orderStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown status: " + status));
        }
    }

    // ── Cancel a pending order ────────────────────────────────────────────────

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId,
                                         @RequestBody Map<String, String> body) {
        try {
            Order order = orderManager.cancelOrder(orderId, body.get("clinicianName"));
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}