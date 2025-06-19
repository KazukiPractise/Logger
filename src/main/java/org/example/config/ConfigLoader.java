package org.example.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {
    public static Map<String, String> loadConfig(String filename) throws IOException {
        Map<String, String> config = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    config.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return config;
    }
}
