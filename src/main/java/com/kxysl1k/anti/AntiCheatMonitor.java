package com.kxysl1k.anti;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.MinecraftClient;

public class AntiCheatMonitor {
    private static final Logger LOGGER = LogManager.getLogger("AntiCheatMonitor");
    private static final AntiCheatMonitor INSTANCE = new AntiCheatMonitor();
    private Thread monitorThread;

    public static AntiCheatMonitor getInstance() { return INSTANCE; }

    public void start() {
        monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    MinecraftClient client = MinecraftClient.getInstance();

                    // Одночасно singleplayer і multiplayer
                    if (client.isInSingleplayer() && client.getCurrentServerEntry() != null) {
                        LOGGER.error("Singleplayer та Multiplayer одночасно! Закриваєм гру.");
                        client.scheduleStop();
                        break;
                    }

                    // Детект ін'єкцій (спрощено)
                    String javaProps = System.getProperty("java.library.path");
                    if (javaProps.contains("cheatengine") || java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-javaagent")) {
                        LOGGER.error("Java агент або CheatEngine виявлено! Закриваєм гру.");
                        client.scheduleStop();
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "SecureJoin-AntiCheat");
        monitorThread.setDaemon(true);
        monitorThread.start();
        LOGGER.info("AntiCheatMonitor started");
    }
}