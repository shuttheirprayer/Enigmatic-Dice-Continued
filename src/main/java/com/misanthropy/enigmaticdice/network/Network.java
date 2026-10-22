package com.misanthropy.enigmaticdice.network;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Network {
    private static final String VERSION = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(EnigmaticDice.id("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private Network() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(DoubleJumpPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DoubleJumpPacket::encode).decoder(DoubleJumpPacket::decode).consumerMainThread(DoubleJumpPacket::handle).add();
        CHANNEL.messageBuilder(TimeAccelerationPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TimeAccelerationPacket::encode).decoder(TimeAccelerationPacket::decode).consumerMainThread(TimeAccelerationPacket::handle).add();
        CHANNEL.messageBuilder(LookAtTargetPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LookAtTargetPacket::encode).decoder(LookAtTargetPacket::decode).consumerMainThread(LookAtTargetPacket::handle).add();
    }
}
