package com.misanthropy.enigmaticdice.compat;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.Protection;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class FtbChunksClaims {
    private FtbChunksClaims() {
    }

    public static boolean isClaimed(ServerLevel level, BlockPos pos) {
        FTBChunksAPI.API api = FTBChunksAPI.api();
        return api.isManagerLoaded() && api.getManager().getChunk(new ChunkDimPos(level, pos)) != null;
    }

    public static boolean canEdit(ServerPlayer player, BlockPos pos) {
        FTBChunksAPI.API api = FTBChunksAPI.api();
        return !api.isManagerLoaded() || !api.getManager().shouldPreventInteraction(player, InteractionHand.MAIN_HAND, pos, Protection.EDIT_BLOCK, null);
    }
}
