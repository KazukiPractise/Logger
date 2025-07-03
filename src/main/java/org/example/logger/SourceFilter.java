package org.example.logger;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SourceFilter implements LogFilter {
    private final Set<String> allowedSources;

    public SourceFilter(String allowedCsv) {
        this.allowedSources = Stream.of(allowedCsv.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean shouldLog(LogEvent event) {
        return allowedSources.contains(event.normalizedSource());
    }
}