package com.misanthropy.enigmaticdice.util;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.network.Network;
import com.misanthropy.enigmaticdice.network.TimeAccelerationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID)
public final class TimeAcceleration {
    private static int multiplier;
    private static int remaining;

    private TimeAcceleration() {
    }

    public static boolean isActive() {
        return remaining > 0;
    }

    public static void start(MinecraftServer server, int multiplier, int duration) {
        TimeAcceleration.multiplier = multiplier;
        TimeAcceleration.remaining = duration;
        Network.CHANNEL.send(PacketDistributor.ALL.noArg(), new TimeAccelerationPacket(multiplier, duration));
    }

    @SubscribeEvent
    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || remaining <= 0) return;
        ServerLevel overworld = event.getServer().overworld();
        if (overworld.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            long time = overworld.getDayTime() + multiplier - 1;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                level.setDayTime(time);
            }
        }
        if (--remaining <= 0) {
            Network.CHANNEL.send(PacketDistributor.ALL.noArg(), new TimeAccelerationPacket(1, 0));
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (remaining > 0 && event.getEntity() instanceof ServerPlayer player) {
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TimeAccelerationPacket(multiplier, remaining));
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        remaining = 0;
    }
}
