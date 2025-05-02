package com.kxysl1k.network;

import com.kxysl1k.util.SystemInfoCollector;
import com.kxysl1k.network.DHKeyExchangeHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import com.kxysl1k.network.ModPayload;


import java.nio.file.Files;

public class ModDataSender {

    public static void sendData() {
        try {
            byte[] pubKey = DHKeyExchangeHandler.getPublicKeyEncoded();
            byte[] encrypted = Files.readAllBytes(new SystemInfoCollector().getConfigPath());
            long timestamp = System.currentTimeMillis();

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(pubKey.length);
            buf.writeBytes(pubKey);
            buf.writeLong(timestamp);
            buf.writeInt(encrypted.length);
            buf.writeBytes(encrypted);

            // 🔄 Надсилання CustomPayload
            ClientPlayNetworking.send(new ModPayload(buf));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
