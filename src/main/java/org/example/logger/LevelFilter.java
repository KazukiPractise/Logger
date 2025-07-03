package org.example.logger;

public class LevelFilter implements LogFilter {
    private final String level;

    public LevelFilter(String level) {
        this.level = level;
    }

    @Override
    public boolean shouldLog(LogEvent event) {
        return event.level().equalsIgnoreCase(level);
    }
}