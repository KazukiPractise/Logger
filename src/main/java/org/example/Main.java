package org.example;

import org.example.config.ConfigLoader;
import org.example.logger.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Map<String, String> config = ConfigLoader.loadConfig("src/main/resources/config.txt");

        String level = config.getOrDefault("log.level", "INFO");
        int threshold = Integer.parseInt(config.getOrDefault("threshold", "3"));
        long muteDuration = Long.parseLong(config.getOrDefault("muteDuration", "10"));
        int minLength = Integer.parseInt(config.getOrDefault("length.min", "5"));
        int maxLength = Integer.parseInt(config.getOrDefault("length.max", "200"));

        SafeLogger logger = SafeLogger.getInstance();
        logger.addFilter(new LevelFilter(level));
        logger.addFilter(new SpamMuteFilter(threshold, muteDuration, TimeUnit.SECONDS));
        logger.addFilter(new LogLengthFilter(minLength, maxLength));

        if (Boolean.parseBoolean(config.getOrDefault("filter.illegalChars", "true"))) {
            logger.addFilter(new IllegalCharacterFilter());
        }

        if (Boolean.parseBoolean(config.getOrDefault("filter.controlChars", "true"))) {
            logger.addFilter(new ControlCharacterFilter());
        }

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = List.of(
                executor.submit(new SafeLogger.LoggingTask("INFO: Booting system")),
                executor.submit(new SafeLogger.LoggingTask("DEBUG: Debug message")),
                executor.submit(new SafeLogger.LoggingTask("INFO: Running diagnostics"))
        );

        for (Future<Void> f : futures) f.get();
        executor.shutdown();
    }
}