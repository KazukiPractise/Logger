package org.example.logger;

public class IllegalCharacterFilter implements LogFilter {
    @Override
    public boolean shouldLog(LogEvent event) {

        return event.message().matches("^[\\p{Print}\\p{Space}]*$");
    }
}