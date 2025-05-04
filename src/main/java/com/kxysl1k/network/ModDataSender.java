package com.kxysl1k.network;

import com.kxysl1k.SecureJoinClientMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;

public record ModDataSender(PacketByteBuf buf) implements CustomPayload {
    public static final CustomPayload.Id<ModDataSender> ID = new CustomPayload.Id<>(
            Identifier.of("securejoin", "mods")
    );

    @Override
    public CustomPayload.Id<ModDataSender> getId() {
        return ID;
    }

    public static void sendData() {
        try {
            // Отримуємо шлях до збереженого .enc файлу з колектора
            Path configPath = SecureJoinClientMod.collector.getConfigPath();

            if (!Files.exists(configPath)) {
                SecureJoinClientMod.LOGGER.warn("Encrypted config not found: {}", configPath);
                return;
            }

            byte[] content = Files.readAllBytes(configPath);
            PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
            buf.writeInt(content.length);
            buf.writeBytes(content);

            ClientPlayNetworking.send(new ModDataSender(buf));
            SecureJoinClientMod.LOGGER.info("Encrypted system info sent to server.");
        } catch (Exception e) {
            SecureJoinClientMod.LOGGER.error("Failed to send encrypted system info", e);
        }
    }
}
