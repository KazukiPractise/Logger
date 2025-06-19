package org.example.logger;

public interface LogFilter {
    boolean shouldLog(LogEvent event);
}




interface EventFilter extends LogFilter {
    boolean shouldLog(LogEvent event);
}
