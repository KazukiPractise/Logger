package org.example.logger;

public class LogLengthFilter implements LogFilter {
    private final int minLength;
    private final int maxLength;

    public LogLengthFilter(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean shouldLog(LogEvent event) {
        int len = event.message().length();
        return len >= minLength && len <= maxLength;
    }
}