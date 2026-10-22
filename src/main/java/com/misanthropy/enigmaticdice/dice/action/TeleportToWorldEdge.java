package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.SafeTeleport;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.border.WorldBorder;

public record TeleportToWorldEdge(int margin) implements DiceAction {
    public static final MapCodec<TeleportToWorldEdge> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("margin", 10).forGetter(TeleportToWorldEdge::margin)
    ).apply(instance, TeleportToWorldEdge::new));

    @Override
    public Result run(DiceContext context) {
        ServerLevel level = context.level();
        RandomSource random = context.random();
        WorldBorder border = level.getWorldBorder();
        BlockPos origin = context.player().blockPosition();
        double half = Math.max(border.getSize() / 2.0 - margin, 0);
        double offset = random.nextBoolean() ? half : -half;
        boolean alongX = random.nextBoolean();
        int x = SafeTeleport.clampHorizontal(alongX ? border.getCenterX() + offset : origin.getX());
        int z = SafeTeleport.clampHorizontal(alongX ? origin.getZ() : border.getCenterZ() + offset);
        SafeTeleport.toSurface(context.player(), level, x, z, context.player().getYRot(), success -> {
            if (success) context.announce();
            else context.fallback();
        });
        return Result.PENDING;
    }
}
