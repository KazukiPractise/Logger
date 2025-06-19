package org.example.logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SpamMuteFilter implements EventFilter {
    private final int threshold;
    private final long muteMillis;
    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    public SpamMuteFilter(int threshold, long duration, TimeUnit unit) {
        this.threshold = threshold;
        this.muteMillis = unit.toMillis(duration);
    }

    @Override
    public boolean shouldLog(LogEvent event) {
        String source = event.normalizedSource();
        counts.putIfAbsent(source, new AtomicInteger(0));
        int count = counts.get(source).incrementAndGet();

        if (count > threshold) {
            SafeLogger.getInstance().muteSource(source, muteMillis);
            return false;
        }
        return true;
    }
}