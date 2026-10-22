package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.Claims;
import com.misanthropy.enigmaticdice.util.Scheduler;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;

public record LightningStorm(int bolts, int radius) implements DiceAction {
    public static final MapCodec<LightningStorm> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("bolts", 15).forGetter(LightningStorm::bolts),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("radius", 8).forGetter(LightningStorm::radius)
    ).apply(instance, LightningStorm::new));

    @Override
    public Result run(DiceContext context) {
        ServerLevel level = context.level();
        BlockPos center = context.player().blockPosition();
        strike(level, center);
        int elapsed = 0;
        for (int i = 0; i < bolts; i++) {
            int delay = 15 + i * 15 - elapsed;
            elapsed += i;
            Scheduler.schedule(delay, () -> {
                strike(level, randomAround(level, center));
                strike(level, randomAround(level, center));
            });
        }
        return Result.DONE;
    }

    private BlockPos randomAround(ServerLevel level, BlockPos center) {
        return center.offset(level.random.nextInt(2 * radius + 1) - radius, 0, level.random.nextInt(2 * radius + 1) - radius);
    }

    private static void strike(ServerLevel level, BlockPos pos) {
        BlockPos target = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        boolean protectedArea = Claims.isClaimed(level, target);
        bolt.setVisualOnly(protectedArea);
        level.addFreshEntity(bolt);
        if (!protectedArea) return;
        AABB area = new AABB(bolt.getX() - 3, bolt.getY() - 3, bolt.getZ() - 3, bolt.getX() + 3, bolt.getY() + 9, bolt.getZ() + 3);
        for (Entity entity : level.getEntities(bolt, area, Entity::isAlive)) {
            if (!ForgeEventFactory.onEntityStruckByLightning(entity, bolt)) entity.thunderHit(level, bolt);
        }
    }
}
