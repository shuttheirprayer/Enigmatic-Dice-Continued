package com.misanthropy.enigmaticdice.network;

import com.misanthropy.enigmaticdice.client.ClientTime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TimeAccelerationPacket(int multiplier, int duration) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(multiplier);
        buffer.writeVarInt(duration);
    }

    public static TimeAccelerationPacket decode(FriendlyByteBuf buffer) {
        return new TimeAccelerationPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTime.start(multiplier, duration));
    }
}
