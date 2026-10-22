package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.Combat;
import com.misanthropy.enigmaticdice.util.SafeSpot;
import com.misanthropy.enigmaticdice.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.OptionalInt;

public record TeleportMonsters(int radius, int interval, double distance) implements DiceAction {
    public static final MapCodec<TeleportMonsters> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("radius", 100).forGetter(TeleportMonsters::radius),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("interval", 10).forGetter(TeleportMonsters::interval),
            Codec.doubleRange(0, 64).optionalFieldOf("distance", 5.0).forGetter(TeleportMonsters::distance)
    ).apply(instance, TeleportMonsters::new));

    @Override
    public boolean canRun(DiceContext context) {
        return !monsters(context).isEmpty();
    }

    @Override
    public Result run(DiceContext context) {
        Deque<Monster> queue = new ArrayDeque<>(monsters(context));
        Scheduler.repeat(1, interval, queue.size(), () -> {
            Monster monster = queue.poll();
            ServerPlayer player = context.onlinePlayer();
            if (monster == null || player == null || !monster.isAlive() || monster.level() != player.level()) return;
            ServerLevel level = player.serverLevel();
            double angle = level.random.nextDouble() * Math.PI * 2;
            int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
            OptionalInt y = SafeSpot.near(level, x, z, player.getBlockY(), 4);
            if (y.isPresent()) {
                monster.teleportTo(x + 0.5, y.getAsInt(), z + 0.5);
            } else {
                monster.teleportTo(player.getX(), player.getY(), player.getZ());
            }
        });
        return Result.DONE;
    }

    private List<Monster> monsters(DiceContext context) {
        return context.level().getEntitiesOfClass(Monster.class, context.player().getBoundingBox().inflate(radius),
                monster -> monster.isAlive() && !Combat.isBoss(monster));
    }
}
