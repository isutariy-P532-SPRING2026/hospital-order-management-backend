package com.healthcare.ordermanagement.pattern.command;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CommandLogEntry {

    private final String commandType;
    private final String orderId;
    private final String actor;
    private final LocalDateTime executedAt;

    public CommandLogEntry(String commandType, String orderId, String actor) {
        this.commandType = commandType;
        this.orderId     = orderId;
        this.actor       = actor;
        this.executedAt  = LocalDateTime.now();
    }
}