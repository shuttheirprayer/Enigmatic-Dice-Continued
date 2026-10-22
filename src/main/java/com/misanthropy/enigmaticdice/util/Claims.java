package com.misanthropy.enigmaticdice.util;

import com.misanthropy.enigmaticdice.compat.FtbChunksClaims;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.ModList;

public final class Claims {
    private static final boolean FTB_CHUNKS = ModList.get().isLoaded("ftbchunks");

    private Claims() {
    }

    public static boolean isClaimed(ServerLevel level, BlockPos pos) {
        if (FTB_CHUNKS) return FtbChunksClaims.isClaimed(level, pos);
        return isBreakDenied(level, pos, FakePlayerFactory.getMinecraft(level));
    }

    public static boolean isAnyClaimed(ServerLevel level, BlockPos center, int radius) {
        int minX = SectionPos.blockToSectionCoord(center.getX() - radius);
        int maxX = SectionPos.blockToSectionCoord(center.getX() + radius);
        int minZ = SectionPos.blockToSectionCoord(center.getZ() - radius);
        int maxZ = SectionPos.blockToSectionCoord(center.getZ() + radius);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (isClaimed(level, new BlockPos(SectionPos.sectionToBlockCoord(x, 8), center.getY(), SectionPos.sectionToBlockCoord(z, 8)))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean canEdit(ServerPlayer player, BlockPos pos) {
        if (FTB_CHUNKS) return FtbChunksClaims.canEdit(player, pos);
        return player.serverLevel().mayInteract(player, pos);
    }

    public static boolean isBreakDenied(ServerLevel level, BlockPos pos, Player breaker) {
        return MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), breaker));
    }
}
