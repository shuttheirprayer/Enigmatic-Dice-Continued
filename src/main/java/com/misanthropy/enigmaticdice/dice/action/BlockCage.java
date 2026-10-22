package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.Claims;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public record BlockCage(Block block, List<Vec3i> offsets) implements DiceAction {
    private static final List<Vec3i> DEFAULT_OFFSETS = List.of(
            new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1),
            new Vec3i(1, 1, 0), new Vec3i(-1, 1, 0), new Vec3i(0, 1, 1), new Vec3i(0, 1, -1),
            new Vec3i(0, 2, 0));

    public static final MapCodec<BlockCage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ForgeRegistries.BLOCKS.getCodec().optionalFieldOf("block", Blocks.ANCIENT_DEBRIS).forGetter(BlockCage::block),
            Vec3i.CODEC.listOf().optionalFieldOf("offsets", DEFAULT_OFFSETS).forGetter(BlockCage::offsets)
    ).apply(instance, BlockCage::new));

    @Override
    public boolean canRun(DiceContext context) {
        BlockPos origin = context.player().blockPosition();
        return offsets.stream().anyMatch(offset -> canPlace(context.level(), origin.offset(offset)));
    }

    @Override
    public Result run(DiceContext context) {
        ServerLevel level = context.level();
        BlockPos origin = context.player().blockPosition();
        BlockState state = block.defaultBlockState();
        for (Vec3i offset : offsets) {
            BlockPos pos = origin.offset(offset);
            if (canPlace(level, pos)) level.setBlock(pos, state, Block.UPDATE_ALL);
        }
        return Result.DONE;
    }

    private static boolean canPlace(ServerLevel level, BlockPos pos) {
        if (!level.isInWorldBounds(pos)) return false;
        BlockState current = level.getBlockState(pos);
        if (current.canBeReplaced()) return true;
        return !current.hasBlockEntity() && current.getDestroySpeed(level, pos) >= 0 && !Claims.isClaimed(level, pos);
    }
}
