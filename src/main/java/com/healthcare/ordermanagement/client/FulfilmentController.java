package com.healthcare.ordermanagement.client;

import com.healthcare.ordermanagement.domain.Order;
import com.healthcare.ordermanagement.manager.OrderManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fulfilment")
@CrossOrigin(origins = "*")
public class FulfilmentController {

    private final OrderManager orderManager;

    public FulfilmentController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    // ── Claim the next available order ────────────────────────────────────────

    @PostMapping("/claim")
    public ResponseEntity<?> claimNextOrder(@RequestBody Map<String, String> body) {
        try {
            Order order = orderManager.claimNextOrder(body.get("staffName"));
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Complete a claimed order ──────────────────────────────────────────────

    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable String orderId,
                                           @RequestBody Map<String, String> body) {
        try {
            Order order = orderManager.completeOrder(orderId, body.get("staffName"));
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}