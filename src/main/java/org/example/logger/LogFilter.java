package org.example.logger;

public interface LogFilter {
    boolean shouldLog(LogEvent event);
}
