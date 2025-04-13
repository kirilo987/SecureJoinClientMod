package org.kxysl1k.securejoinclientmod.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SecureJoinClientMod implements ClientModInitializer {

    private static final Identifier CHANNEL = new Identifier("securejoin", "mods");
    private static final Gson gson = new Gson();
    private static boolean inMultiplayer = false;
    private static boolean inSingleplayer = false;

    private static Path hiddenDir;
    private static Path secretFile;

    static {
        // Видалення файлу при виході з гри
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (secretFile != null && Files.exists(secretFile)) {
                    Files.delete(secretFile);
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to delete secure JSON on shutdown: " + e.getMessage());
            }
        }));
    }

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (inSingleplayer) {
                client.inGameHud.getChatHud().addMessage(net.minecraft.text.Text.of("❌ You cannot join servers while in singleplayer!"));
                client.disconnect();
                return;
            }

            inMultiplayer = true;
            String json = generateClientInfoJson();
            saveJsonToFile(json);

            PacketByteBuf buf = new PacketByteBuf(net.minecraft.network.PacketByteBufs.create());
            buf.writeBytes(json.getBytes(StandardCharsets.UTF_8));
            ClientPlayNetworking.send(CHANNEL, buf);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (inMultiplayer) {
                MinecraftClient.getInstance().scheduleStop();
                return;
            }
            inSingleplayer = true;
            String json = generateClientInfoJson();
            saveJsonToFile(json);
        });
    }

    private String generateClientInfoJson() {
        MinecraftClient client = MinecraftClient.getInstance();
        Session session = client.getSession();

        JsonObject info = new JsonObject();
        info.addProperty("username", session.getUsername());
        info.addProperty("uuid", session.getUuidOrNull() != null ? session.getUuidOrNull().toString() : "null");
        info.addProperty("minecraftVersion", client.getGame().getVersion().getName());
        info.addProperty("os", System.getProperty("os.name"));
        info.addProperty("javaVersion", System.getProperty("java.version"));
        info.addProperty("language", client.getLanguageManager().getLanguage());
        info.addProperty("clientBrand", client.getGame().getVersion().getId());

        JsonArray modsArray = new JsonArray();
        FabricLoader.getInstance().getAllMods().forEach(mod -> {
            JsonObject modEntry = new JsonObject();
            modEntry.addProperty("modId", mod.getMetadata().getId());
            modEntry.addProperty("version", mod.getMetadata().getVersion().getFriendlyString());
            modsArray.add(modEntry);
        });

        info.add("mods", modsArray);
        return gson.toJson(info);
    }

    private void saveJsonToFile(String json) {
        try {
            // Створення прихованої директорії у .minecraft/cache/.sjoin/
            if (hiddenDir == null) {
                hiddenDir = FabricLoader.getInstance().getGameDir().resolve("cache").resolve(".sjoin");
                Files.createDirectories(hiddenDir);
                hiddenDir.toFile().setReadable(false, false); // Приховати доступ
            }

            // Генерація або збереження ім'я файла
            if (secretFile == null) {
                String randomName = generateRandomFileName();
                secretFile = hiddenDir.resolve(randomName + ".json");
            }

            // Якщо файл існує — видалити
            if (Files.exists(secretFile)) {
                Files.delete(secretFile);
            }

            Files.writeString(secretFile, json);
            secretFile.toFile().setReadOnly(); // Робимо файл доступним лише для читання
        } catch (IOException e) {
            System.err.println("❌ Failed to save hidden JSON: " + e.getMessage());
        }
    }

    private String generateRandomFileName() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}