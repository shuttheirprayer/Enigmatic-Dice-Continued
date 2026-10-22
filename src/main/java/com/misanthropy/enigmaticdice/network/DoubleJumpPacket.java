package com.misanthropy.enigmaticdice.network;

import com.misanthropy.enigmaticdice.item.GravityCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DoubleJumpPacket() {
    public void encode(FriendlyByteBuf buffer) {
    }

    public static DoubleJumpPacket decode(FriendlyByteBuf buffer) {
        return new DoubleJumpPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) GravityCore.airJump(player);
    }
}
