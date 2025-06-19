// LogEvent.java
package org.example.logger;

public record LogEvent(String level, String message, String source) {
    public String normalizedSource() {
        return source != null ? source.trim().toUpperCase() : "UNKNOWN";
    }
}