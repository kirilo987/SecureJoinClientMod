package com.kxysl1k.util;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public class SystemInfoCollector {
    private static final Logger LOGGER = LogManager.getLogger("SystemInfoCollector");
    private final Path configPath;

    public SystemInfoCollector() {
        String filename = UUID.randomUUID().toString().replaceAll("-", "") + ".enc";
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(filename);
    }

    public Path getConfigPath() {
        return configPath;
    }

    public void collectAndEncrypt() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("player_uuid", net.minecraft.client.MinecraftClient.getInstance().getSession().getXuid());
            data.put("mc_version", net.minecraft.SharedConstants.getGameVersion().getName());
            data.put("os", System.getProperty("os.name"));

            List<Map<String, String>> mods = FabricLoader.getInstance().getAllMods().stream().map(container -> {
                Map<String, String> mod = new HashMap<>();
                mod.put("id", container.getMetadata().getId());
                mod.put("name", container.getMetadata().getName());
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    // Use the first path from getPaths()
                    Optional<Path> firstPath = container.getOrigin().getPaths().stream().findFirst();
                    if (firstPath.isPresent()) {
                        byte[] bytes = java.nio.file.Files.readAllBytes(firstPath.get());
                        mod.put("hash", bytesToHex(md.digest(bytes)));
                    } else {
                        mod.put("hash", "");
                    }
                } catch (Exception e) {
                    mod.put("hash", "");
                }
                return mod;
            }).collect(Collectors.toList());
            data.put("mods", mods);

            String json = new Gson().toJson(data);
            byte[] encrypted = CryptoUtils.encrypt(json.getBytes());
            byte[] hmac = HmacUtils.hmac(encrypted);

            try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                fos.write(encrypted);
                fos.write(hmac);
            }
            LOGGER.info("Config encrypted and saved to {}", configPath);
        } catch (Exception e) {
            LOGGER.error("Error in collectAndEncrypt", e);
        }
    }

    public void deleteConfig() {
        try {
            java.nio.file.Files.deleteIfExists(configPath);
            LOGGER.info("Deleted config: {}", configPath);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete config", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}