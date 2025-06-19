package org.example.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class SafeLogger {
    private static final Logger log = LogManager.getLogger(SafeLogger.class);
    private static final ReentrantLock lock = new ReentrantLock();
    private static SafeLogger instance;

    private final List<LogFilter> filters = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, Long> mutedSources = new ConcurrentHashMap<>();

    private SafeLogger() {}

    public static SafeLogger getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new SafeLogger();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public void addFilter(LogFilter filter) {
        filters.add(filter);
    }

    public void muteSource(String source, long millis) {
        mutedSources.put(source, System.currentTimeMillis() + millis);
    }

    public boolean isMuted(String source) {
        Long unmuteTime = mutedSources.get(source);
        return unmuteTime != null && unmuteTime > System.currentTimeMillis();
    }

    public void log(String message, String source) {
        LogEvent event = parse(message, source);
        if (isMuted(event.normalizedSource())) return;

        for (LogFilter filter : filters) {
            if (filter instanceof EventFilter && !((EventFilter) filter).shouldLog(event)) {
                return;
            }
        }
        log.info("{} - {}", event.level(), event.message());
    }

    private LogEvent parse(String message, String source) {
        String[] parts = message.split(":", 2);
        String level = parts.length > 1 ? parts[0] : "INFO";
        String msg = parts.length > 1 ? parts[1].trim() : message;
        return new LogEvent(level.trim(), msg, source);
    }

    public static class LoggingTask implements Callable<Void> {
        private final String message;

        public LoggingTask(String message) {
            this.message = message;
        }

        @Override
        public Void call() {
            SafeLogger.getInstance().log(message, Thread.currentThread().getName());
            return null;
        }
    }
}