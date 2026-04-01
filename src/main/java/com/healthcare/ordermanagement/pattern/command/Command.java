package com.healthcare.ordermanagement.pattern.command;

import java.time.LocalDateTime;

public interface Command {
    void execute();
    void undo();              
    String getCommandType();
    String getOrderId();
    String getActor();
    LocalDateTime getExecutedAt();
}