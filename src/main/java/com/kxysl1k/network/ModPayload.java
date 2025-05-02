package com.kxysl1k.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ModPayload(PacketByteBuf buf) implements CustomPayload {
    public static final Id<ModPayload> ID = new CustomPayload.Id<>(Identifier.of("securejoin", "mods"));

    @Override
    public Id<? extends CustomPayload> id() {
        return ID;
    }
}
