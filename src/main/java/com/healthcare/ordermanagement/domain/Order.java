package com.healthcare.ordermanagement.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Order {

    private String orderId;
    private OrderType type;
    private String patientName;
    private String clinicianName;
    private String description;
    private Priority priority;
    private OrderStatus status;
    private LocalDateTime submittedAt;
    private String claimedBy;

    public Order(String orderId,
                 OrderType type,
                 String patientName,
                 String clinicianName,
                 String description,
                 Priority priority) {
        this.orderId      = orderId;
        this.type         = type;
        this.patientName  = patientName;
        this.clinicianName = clinicianName;
        this.description  = description;
        this.priority     = priority;
        this.status       = OrderStatus.PENDING;
        this.submittedAt  = LocalDateTime.now();
        this.claimedBy    = null;
    }
}