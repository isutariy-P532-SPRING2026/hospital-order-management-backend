package com.healthcare.ordermanagement.client;

import com.healthcare.ordermanagement.manager.OrderManager;
import com.healthcare.ordermanagement.pattern.command.CommandLogEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    private final OrderManager orderManager;

    public AuditController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    // ── Get full audit trail ──────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<CommandLogEntry>> getAuditLog() {
        return ResponseEntity.ok(orderManager.getCommandLog());
    }


    @PostMapping("/undo")
    public ResponseEntity<?> undoLastCommand() {
        boolean success = orderManager.undoLastCommand();
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Last command undone successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Nothing to undo"));
    }
}