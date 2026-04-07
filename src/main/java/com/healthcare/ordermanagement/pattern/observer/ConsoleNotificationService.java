package com.healthcare.ordermanagement.pattern.observer;

import com.healthcare.ordermanagement.domain.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ConsoleNotificationService implements NotificationService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void notify(Order order, String event) {
        String recipient = resolveRecipient(order, event);
        String change    = resolveChange(event, order);

        System.out.printf(
            "[NOTIFICATION] %s | Event: %-30s | Notified: %-25s | " +
            "Order: %-14s | Type: %-10s | Patient: %-20s | " +
            "Priority: %-8s | Changed: %s%n",
            FMT.format(LocalDateTime.now()),
            event,
            recipient,
            order.getOrderId(),
            order.getType(),
            order.getPatientName(),
            order.getPriority(),
            change
        );
    }

    // ── Who receives the notification per event ───────────────────────────────

    private String resolveRecipient(Order order, String event) {
        return switch (event) {
            case "ORDER_SUBMITTED"          -> "Fulfilment Dept";
            case "ORDER_CLAIMED"            -> order.getClinicianName();
            case "ORDER_COMPLETED"          -> order.getClinicianName()
                                               + " & " + order.getPatientName();
            case "ORDER_CANCELLED"          -> "Fulfilment Dept";
            case "ORDER_SUBMISSION_UNDONE"  -> "Fulfilment Dept";
            case "ORDER_CLAIM_UNDONE"       -> order.getClinicianName();
            case "ORDER_COMPLETION_UNDONE"  -> order.getClinicianName();
            case "ORDER_CANCELLATION_UNDONE"-> "Fulfilment Dept";
            default                         -> "System";
        };
    }

    // ── What changed      ──────────────────────────────

    private String resolveChange(String event, Order order) {
        return switch (event) {
            case "ORDER_SUBMITTED"           -> "New order placed — status: PENDING";
            case "ORDER_CLAIMED"             -> "Order claimed by " + order.getClaimedBy()
                                                + " — status: IN_PROGRESS";
            case "ORDER_COMPLETED"           -> "Order completed by " + order.getClaimedBy()
                                                + " — status: COMPLETED";
            case "ORDER_CANCELLED"           -> "Order cancelled — status: CANCELLED";
            case "ORDER_SUBMISSION_UNDONE"   -> "Submission reversed — order removed";
            case "ORDER_CLAIM_UNDONE"        -> "Claim released — status: PENDING";
            case "ORDER_COMPLETION_UNDONE"   -> "Completion reversed — status: IN_PROGRESS";
            case "ORDER_CANCELLATION_UNDONE" -> "Cancellation reversed — status: PENDING";
            default                          -> "Status: " + order.getStatus();
        };
    }
}