package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigLoader;
import org.example.logger.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Main {
    private static final Logger consoleLog = LogManager.getLogger("ConsoleLogger");

    private static final String SOURCE_SYSTEM = "SYSTEM";
    private static final String SOURCE_USER = "USER";
    private static final String SOURCE_ATTACKER = "ATTACKER";
    private static final String SOURCE_GUEST = "GUEST";

    public static void main(String[] args) throws Exception {
        Map<String, String> config = ConfigLoader.loadConfig("src/main/resources/config.txt");

        String level = config.getOrDefault("log.level", "INFO");
        int minLength = Integer.parseInt(config.getOrDefault("length.min", "5"));
        int maxLength = Integer.parseInt(config.getOrDefault("length.max", "200"));
        String allowedSourcesCsv = config.getOrDefault("sources.allowed", "SYSTEM,USER");

        SafeLogger logger = SafeLogger.getInstance();

        logger.addFilter(new LevelFilter(level));
        logger.addFilter(new LogLengthFilter(minLength, maxLength));
        logger.addFilter(new SourceFilter(allowedSourcesCsv));

        if (Boolean.parseBoolean(config.getOrDefault("filter.illegalChars", "true"))) {
            logger.addFilter(new IllegalCharacterFilter());
        }

        if (Boolean.parseBoolean(config.getOrDefault("filter.controlChars", "true"))) {
            logger.addFilter(new ControlCharacterFilter());
        }

        consoleLog.info("--- HELLO WORLD ---");

        List<Future<Void>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
            consoleLog.info("Expected: 3 logs (DEBUG is filtered by LevelFilter)");
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Booting system", SOURCE_SYSTEM)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("DEBUG: Debug message from user", SOURCE_USER)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Running diagnostics", SOURCE_SYSTEM)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: User login successful", SOURCE_USER)));

            consoleLog.info("Expected: 1 log (Unauthorized source is filtered)");
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Message from authorized source", SOURCE_USER)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Message from unauthorized source", SOURCE_GUEST)));

            consoleLog.info("Expected: 2 logs (Too short/long are filtered)");
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Short", SOURCE_SYSTEM)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Medium log message example", SOURCE_USER)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: This is a very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very useful.", SOURCE_USER)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: A", SOURCE_SYSTEM)));

            consoleLog.info("Expected: 1 log (Control chars are filtered)");
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Message without special chars", SOURCE_SYSTEM)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Message with \n new line", SOURCE_USER)));
            futures.add(executor.submit(new SafeLogger.LoggingTask("INFO: Message with \u0007 bell character", SOURCE_USER)));

            consoleLog.info("--- Waiting for all tasks to complete ---");
            int completedTasks = 0;
            for (Future<Void> f : futures) {
                try {
                    f.get();
                    completedTasks++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    consoleLog.error("Task interrupted: {}", e.getMessage());
                } catch (ExecutionException e) {
                    consoleLog.error("Task failed with exception: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                }
            }
            consoleLog.info("--- All " + completedTasks + " tasks submitted to the executor have finished processing ---");

        }
        consoleLog.info("--- GOODBYE WORLD ---");
        consoleLog.info("Main thread finished: " + Thread.currentThread().getName());
    }
}