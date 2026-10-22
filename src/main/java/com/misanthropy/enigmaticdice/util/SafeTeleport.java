package com.misanthropy.enigmaticdice.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;

public final class SafeTeleport {
    private static final int TIMEOUT = 600;
    private static final TicketType<ChunkPos> TICKET = TicketType.create("enigmaticdice_teleport", Comparator.comparingLong(ChunkPos::toLong), TIMEOUT);

    private SafeTeleport() {
    }

    public static void toSurface(ServerPlayer player, ServerLevel level, int x, int z, float yaw, Consumer<Boolean> result) {
        MinecraftServer server = level.getServer();
        UUID playerId = player.getUUID();
        ChunkPos chunk = new ChunkPos(x >> 4, z >> 4);
        level.getChunkSource().addRegionTicket(TICKET, chunk, 1, chunk);
        Scheduler.poll(TIMEOUT, () -> {
            if (level.getChunkSource().getChunkNow(chunk.x, chunk.z) == null) return false;
            ServerPlayer target = server.getPlayerList().getPlayer(playerId);
            if (target == null || !target.isAlive()) {
                result.accept(false);
                return true;
            }
            OptionalInt y = SafeSpot.surface(level, x, z);
            if (y.isEmpty()) {
                result.accept(false);
                return true;
            }
            teleport(target, level, x + 0.5, y.getAsInt(), z + 0.5, yaw);
            result.accept(true);
            return true;
        }, () -> result.accept(false));
    }

    public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw) {
        player.teleportTo(level, x, y, z, yaw, player.getXRot());
        player.resetFallDistance();
    }

    public static int clampHorizontal(double value) {
        return (int) Math.max(-Level.MAX_LEVEL_SIZE + 26, Math.min(Level.MAX_LEVEL_SIZE - 26, value));
    }
}
