package com.kxysl1k.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ModPayload(PacketByteBuf buf) implements CustomPayload {

    public static final Id<ModPayload> ID = new Id<>(
            Identifier.of("securejoin", "mods"), ModPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
