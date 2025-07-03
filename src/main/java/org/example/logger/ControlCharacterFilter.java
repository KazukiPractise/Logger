package org.example.logger;

public class ControlCharacterFilter implements LogFilter {
    @Override
    public boolean shouldLog(LogEvent event) {
        String msg = event.message();
        return !(msg.contains("\t") || msg.contains("\n") || msg.contains("\r"));
    }
}