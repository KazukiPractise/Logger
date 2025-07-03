package org.example.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;

public class SafeLogger {
    private static final Logger log = LogManager.getLogger(SafeLogger.class);

    private static class SingletonHolder {
        private static final SafeLogger INSTANCE = new SafeLogger();
    }

    private final List<LogFilter> filters = new CopyOnWriteArrayList<>();

    private SafeLogger() {}

    public static SafeLogger getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addFilter(LogFilter filter) {
        filters.add(filter);
    }

    public void log(String message, String source) {
        LogEvent event = parse(message, source);

        for (LogFilter filter : filters) {
            if (!filter.shouldLog(event)) {
                log.debug("Event skipped by filter: {} for event: {}", filter.getClass().getSimpleName(), event);
                return;
            }
        }
        log.info("{} - {}", event.level(), event.message());
    }

    private LogEvent parse(String message, String source) {
        String[] parts = message.split(":", 2);
        String level = parts.length > 1 ? parts[0].trim() : "INFO";
        String msg = parts.length > 1 ? parts[1].trim() : message;
        return new LogEvent(level, msg, source);
    }

    public static class LoggingTask implements Callable<Void> {
        private final String message;
        private final String source;

        public LoggingTask(String message, String source) {
            this.message = message;
            this.source = source;
        }

        @Override
        public Void call() {
            SafeLogger.getInstance().log(message, source);
            return null;
        }
    }
}