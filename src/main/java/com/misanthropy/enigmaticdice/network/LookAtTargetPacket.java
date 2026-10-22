package com.misanthropy.enigmaticdice.network;

import com.misanthropy.enigmaticdice.client.ClientLook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record LookAtTargetPacket(UUID target, int duration) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(target);
        buffer.writeVarInt(duration);
    }

    public static LookAtTargetPacket decode(FriendlyByteBuf buffer) {
        return new LookAtTargetPacket(buffer.readUUID(), buffer.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientLook.start(target, duration));
    }
}
