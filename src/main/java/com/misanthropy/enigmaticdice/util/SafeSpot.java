package com.misanthropy.enigmaticdice.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.OptionalInt;

public final class SafeSpot {
    private SafeSpot() {
    }

    public static OptionalInt surface(Level level, int x, int z) {
        int min = level.getMinBuildHeight();
        int top = level.dimensionType().hasCeiling()
                ? Math.min(min + level.dimensionType().logicalHeight(), level.getMaxBuildHeight()) - 1
                : level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        return scan(level, x, z, top, min + 1);
    }

    public static OptionalInt near(Level level, int x, int z, int y, int range) {
        int top = Math.min(y + range, level.getMaxBuildHeight() - 2);
        int bottom = Math.max(y - range, level.getMinBuildHeight() + 1);
        return scan(level, x, z, top, bottom);
    }

    private static OptionalInt scan(Level level, int x, int z, int top, int bottom) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top; y >= bottom; y--) {
            if (isStandable(level, pos.set(x, y, z))) return OptionalInt.of(y);
        }
        return OptionalInt.empty();
    }

    public static boolean isStandable(Level level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState ground = level.getBlockState(below);
        if (ground.getFluidState().is(FluidTags.LAVA) || ground.is(BlockTags.FIRE) || ground.is(BlockTags.CAMPFIRES)) return false;
        boolean solid = ground.isFaceSturdy(level, below, Direction.UP);
        boolean water = ground.getFluidState().is(FluidTags.WATER) && ground.getFluidState().isSource();
        return (solid || water) && isOpen(level, feet) && isOpen(level, feet.above());
    }

    public static boolean isOpen(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty() && !state.is(BlockTags.FIRE);
    }
}
