package com.healthcare.ordermanagement.pattern.command;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CommandLog {

    private final List<CommandLogEntry> log = new ArrayList<>();
    private final List<Command> undoStack   = new ArrayList<>();

    public void record(Command command) {
        log.add(new CommandLogEntry(
            command.getCommandType(),
            command.getOrderId(),
            command.getActor()
        ));
        undoStack.add(command);
    }

    public List<CommandLogEntry> getAll() {
        return Collections.unmodifiableList(log);
    }

    
    public boolean undoLast() {
        if (undoStack.isEmpty()) return false;
        Command last = undoStack.remove(undoStack.size() - 1);
        last.undo();
        return true;
    }
}